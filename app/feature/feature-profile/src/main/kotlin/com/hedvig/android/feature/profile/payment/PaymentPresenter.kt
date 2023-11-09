package com.hedvig.android.feature.profile.payment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.apollo.format
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.forever.CampaignCode
import com.hedvig.android.data.forever.ForeverRepository
import com.hedvig.android.data.payment.PaymentData
import com.hedvig.android.data.payment.PaymentRepository
import com.hedvig.android.language.LanguageService
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import giraffe.type.PayoutMethodStatus
import giraffe.type.TypeOfContract
import java.time.LocalDate
import java.util.Locale

internal class PaymentPresenter(
  private val referralsRepositoryProvider: Provider<ForeverRepository>,
  private val paymentRepositoryProvider: Provider<PaymentRepository>,
  private val languageService: LanguageService,
) : MoleculePresenter<PaymentEvent, PaymentUiState> {
  @Composable
  override fun MoleculePresenterScope<PaymentEvent>.present(lastState: PaymentUiState): PaymentUiState {
    var paymentUiState by remember { mutableStateOf(lastState) }
    var newDiscountCodeBeingSubmitted: CampaignCode? by remember { mutableStateOf(null) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      paymentUiState = paymentUiState.copy(isLoading = true, errorMessage = null)
      paymentRepositoryProvider.provide().getPaymentData().fold(
        ifLeft = { error -> paymentUiState = paymentUiState.copy(errorMessage = error.message, isLoading = false) },
        ifRight = { paymentData -> paymentUiState = paymentData.toUiState(languageService.getLocale()) },
      )
    }

    LaunchedEffect(newDiscountCodeBeingSubmitted) {
      val newCampaignCode = newDiscountCodeBeingSubmitted ?: return@LaunchedEffect
      referralsRepositoryProvider.provide().redeemReferralCode(newCampaignCode).fold(
        ifLeft = { error -> paymentUiState = paymentUiState.copy(discountError = error.message) },
        ifRight = { loadIteration++ },
      )
      newDiscountCodeBeingSubmitted = null
    }

    CollectEvents { event ->
      when (event) {
        PaymentEvent.Retry -> loadIteration++
        is PaymentEvent.EditDiscountCode -> {
          paymentUiState = paymentUiState.copy(discountCode = event.discountCode, discountError = null)
        }
        is PaymentEvent.SubmitNewDiscountCode -> newDiscountCodeBeingSubmitted = event.discountCode
      }
    }

    return paymentUiState
  }
}

internal sealed interface PaymentEvent {
  data object Retry : PaymentEvent

  data class EditDiscountCode(val discountCode: CampaignCode) : PaymentEvent

  data class SubmitNewDiscountCode(val discountCode: CampaignCode) : PaymentEvent
}

internal data class PaymentUiState(
  val nextChargeAmount: String? = null,
  val nextChargeDate: LocalDate? = null,
  val monthlyCost: String? = null,
  val insuranceCosts: List<InsuranceCost> = emptyList(),
  val totalDiscount: String? = null,
  val activeDiscounts: List<Discount> = emptyList(),
  val paymentMethod: PaymentMethod? = null,
  val discountCode: CampaignCode? = null,
  val discountError: String? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = false,
  val payoutStatus: PayoutStatus? = null,
) {
  data class InsuranceCost(
    val displayName: String,
    val cost: String?,
    val typeOfContract: TypeOfContract,
  )

  data class PaymentMethod(
    val displayName: String?,
    val displayValue: String?,
  )

  data class Discount(
    val code: String,
    val displayName: String,
  )

  enum class PayoutStatus {
    ACTIVE,
    PENDING,
    NEEDS_SETUP,
  }
}

private fun PaymentData.toUiState(locale: Locale): PaymentUiState {
  val paymentMethod = paymentMethod
  val bankAccount = bankAccount
  return PaymentUiState(
    nextChargeAmount = nextCharge.format(locale),
    monthlyCost = monthlyCost?.format(locale),
    nextChargeDate = nextChargeDate,
    insuranceCosts = contracts.map {
      PaymentUiState.InsuranceCost(
        displayName = it.name,
        typeOfContract = it.typeOfContract,
        cost = null,
      )
    },
    totalDiscount = totalDiscount?.negate()?.format(locale),
    activeDiscounts = redeemedCampagins.map {
      PaymentUiState.Discount(
        code = it.code,
        displayName = it.displayValue ?: "-",
      )
    },
    paymentMethod = when {
      paymentMethod != null -> PaymentUiState.PaymentMethod(
        displayName = when (paymentMethod) {
          is PaymentData.PaymentMethod.CardPaymentMethod -> paymentMethod.brand ?: "Unknown"
          is PaymentData.PaymentMethod.ThirdPartyPaymentMethd -> paymentMethod.name
        },
        displayValue = when (paymentMethod) {
          is PaymentData.PaymentMethod.CardPaymentMethod -> paymentMethod.lastFourDigits
          is PaymentData.PaymentMethod.ThirdPartyPaymentMethd -> paymentMethod.type
        },
      )
      bankAccount != null -> PaymentUiState.PaymentMethod(
        displayName = bankAccount.name,
        displayValue = bankAccount.accountNumber,
      )
      else -> null
    },
    payoutStatus = when (payoutMethodStatus) {
      PayoutMethodStatus.ACTIVE -> PaymentUiState.PayoutStatus.ACTIVE
      PayoutMethodStatus.PENDING -> PaymentUiState.PayoutStatus.PENDING
      PayoutMethodStatus.NEEDS_SETUP -> PaymentUiState.PayoutStatus.NEEDS_SETUP
      PayoutMethodStatus.UNKNOWN__ -> null
      null -> null
    },
  )
}
