package com.hedvig.android.feature.profile.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.apollo.format
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.forever.CampaignCode
import com.hedvig.android.data.forever.ForeverRepository
import com.hedvig.android.language.LanguageService
import com.hedvig.android.payment.PaymentData
import com.hedvig.android.payment.PaymentRepository
import giraffe.type.PayoutMethodStatus
import giraffe.type.TypeOfContract
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

internal class PaymentViewModel(
  private val referralsRepositoryProvider: Provider<ForeverRepository>,
  private val paymentRepositoryProvider: Provider<PaymentRepository>,
  val languageService: LanguageService,
) : ViewModel() {

  private val _uiState = MutableStateFlow(PaymentUiState())
  val uiState: StateFlow<PaymentUiState> = _uiState

  data class PaymentUiState(
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

  init {
    loadPaymentData()
  }

  private fun loadPaymentData() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      paymentRepositoryProvider.provide().getPaymentData().fold(
        ifLeft = { error -> _uiState.update { it.copy(errorMessage = error.message, isLoading = false) } },
        ifRight = { paymentData -> _uiState.value = paymentData.toUiState(languageService.getLocale()) },
      )
    }
  }

  fun onDiscountCodeChanged(code: CampaignCode) {
    _uiState.update { it.copy(discountCode = code, discountError = null) }
  }

  fun onDiscountCodeAdded() {
    viewModelScope.launch {
      val code = _uiState.value.discountCode ?: return@launch
      referralsRepositoryProvider.provide().redeemReferralCode(code)
        .fold(
          ifLeft = { error -> _uiState.update { it.copy(discountError = error.message) } },
          ifRight = { loadPaymentData() },
        )
    }
  }

  fun retry() {
    loadPaymentData()
  }
}

private fun PaymentData.toUiState(locale: Locale): PaymentViewModel.PaymentUiState {
  val paymentMethod = paymentMethod
  val bankAccount = bankAccount
  return PaymentViewModel.PaymentUiState(
    nextChargeAmount = nextCharge.format(locale),
    monthlyCost = monthlyCost?.format(locale),
    nextChargeDate = nextChargeDate,
    insuranceCosts = contracts.map {
      PaymentViewModel.PaymentUiState.InsuranceCost(
        displayName = it.name,
        typeOfContract = it.typeOfContract,
        cost = null,
      )
    },
    totalDiscount = totalDiscount?.negate()?.format(locale),
    activeDiscounts = redeemedCampagins.map {
      PaymentViewModel.PaymentUiState.Discount(
        code = it.code,
        displayName = it.displayValue ?: "-",
      )
    },
    paymentMethod = when {
      paymentMethod != null -> PaymentViewModel.PaymentUiState.PaymentMethod(
        displayName = when (paymentMethod) {
          is PaymentData.PaymentMethod.CardPaymentMethod -> paymentMethod.brand ?: "Unknown"
          is PaymentData.PaymentMethod.ThirdPartyPaymentMethd -> paymentMethod.name
        },
        displayValue = when (paymentMethod) {
          is PaymentData.PaymentMethod.CardPaymentMethod -> paymentMethod.lastFourDigits
          is PaymentData.PaymentMethod.ThirdPartyPaymentMethd -> paymentMethod.type
        },
      )

      bankAccount != null -> PaymentViewModel.PaymentUiState.PaymentMethod(
        displayName = bankAccount.name,
        displayValue = bankAccount.accountNumber,
      )

      else -> null
    },
    payoutStatus = when (payoutMethodStatus) {
      PayoutMethodStatus.ACTIVE -> PaymentViewModel.PaymentUiState.PayoutStatus.ACTIVE
      PayoutMethodStatus.PENDING -> PaymentViewModel.PaymentUiState.PayoutStatus.PENDING
      PayoutMethodStatus.NEEDS_SETUP -> PaymentViewModel.PaymentUiState.PayoutStatus.NEEDS_SETUP
      PayoutMethodStatus.UNKNOWN__ -> null
      null -> null
    },
  )
}
