package com.hedvig.android.feature.payments

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.payments.data.CampaignCode
import com.hedvig.android.feature.payments.data.PaymentData
import com.hedvig.android.feature.payments.data.PaymentRepository
import com.hedvig.android.feature.payments.data.RedeemFailure
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate

internal class PaymentPresenter(
  private val paymentRepositoryProvider: Provider<PaymentRepository>,
) : MoleculePresenter<PaymentEvent, PaymentUiState> {
  @Composable
  override fun MoleculePresenterScope<PaymentEvent>.present(lastState: PaymentUiState): PaymentUiState {
    var paymentUiState: PaymentUiState by remember { mutableStateOf(lastState) }
    var newCampaignCodeBeingSubmitted: CampaignCode? by remember { mutableStateOf(null) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      if (paymentUiState is PaymentUiState.Error) {
        paymentUiState = PaymentUiState.Loading
      }
      paymentRepositoryProvider.provide().getPaymentData().fold(
        ifLeft = { _ -> paymentUiState = PaymentUiState.Error },
        ifRight = { paymentData -> paymentUiState = paymentData.toUiState() },
      )
    }

    LaunchedEffect(newCampaignCodeBeingSubmitted) {
      val oldUiState = paymentUiState as? PaymentUiState.Content ?: return@LaunchedEffect
      val oldCampaignsStatus = oldUiState.campaignsStatus as? PaymentUiState.Content.CampaignsStatus.AddNewCampaignCode
        ?: return@LaunchedEffect
      val newCampaignCode = newCampaignCodeBeingSubmitted ?: return@LaunchedEffect
      delay(2_000) // remove this
      paymentRepositoryProvider.provide().redeemReferralCode(newCampaignCode).fold(
        ifLeft = { error: RedeemFailure ->
          paymentUiState = oldUiState.copy(
            campaignsStatus = oldCampaignsStatus.copy(
              discountError = PaymentUiState.Content.CampaignsStatus.AddNewCampaignCode.DiscountError(
                error.safeCast<RedeemFailure.UserFailure>()?.errorMessage,
              ),
            ),
          )
        },
        ifRight = { loadIteration++ },
      )
      newCampaignCodeBeingSubmitted = null
    }

    CollectEvents { event ->
      when (event) {
        PaymentEvent.Retry -> loadIteration++
        is PaymentEvent.EditDiscountCode -> {
          val oldUiState = paymentUiState
          if (oldUiState is PaymentUiState.Content) {
            if (oldUiState.campaignsStatus is PaymentUiState.Content.CampaignsStatus.AddNewCampaignCode) {
              paymentUiState = oldUiState.copy(
                campaignsStatus = oldUiState.campaignsStatus.copy(
                  discountCodeInput = event.discountCode,
                  discountError = null,
                ),
              )
            }
          }
        }

        is PaymentEvent.SubmitNewDiscountCode -> {
          if (newCampaignCodeBeingSubmitted == null) {
            val discountCode = event.discountCode
            if (discountCode.isNotBlank()) {
              newCampaignCodeBeingSubmitted = CampaignCode(discountCode)
            }
          }
        }
      }
    }

    return if (newCampaignCodeBeingSubmitted != null) {
      val paymentUiStateValue = paymentUiState.safeCast<PaymentUiState.Content>() ?: return paymentUiState
      val addNewCampaignCode = paymentUiStateValue
        .campaignsStatus
        .safeCast<PaymentUiState.Content.CampaignsStatus.AddNewCampaignCode>() ?: return paymentUiState
      paymentUiStateValue.copy(
        campaignsStatus = addNewCampaignCode.copy(
          isSubmittingNewCampaignCode = true,
        ),
      )
    } else {
      paymentUiState
    }
  }
}

internal sealed interface PaymentEvent {
  data object Retry : PaymentEvent

  data class EditDiscountCode(val discountCode: String) : PaymentEvent

  data class SubmitNewDiscountCode(val discountCode: String) : PaymentEvent
}

internal sealed interface PaymentUiState {
  data object Loading : PaymentUiState

  data object Error : PaymentUiState

  data class Content(
    val nextChargeStatus: NextChargeStatus,
    val netMonthlyCost: UiMoney,
    val monthlyCostDiscount: UiMoney?,
    val insuranceCosts: ImmutableList<InsuranceCost>,
    val campaignsStatus: CampaignsStatus,
    val paymentDetails: PaymentDetails,
  ) : PaymentUiState {
    sealed interface NextChargeStatus {
      val nextCharge: UiMoney

      data class UpcomingCharge(
        override val nextCharge: UiMoney,
        val nextChargeDate: LocalDate,
      ) : NextChargeStatus

      // When there is no known upcoming charge, we simply show the monthly cost
      data class NoUpcomingCharge(
        val monthlyCost: UiMoney,
      ) : NextChargeStatus {
        override val nextCharge: UiMoney = monthlyCost
      }
    }

    data class InsuranceCost(
      val displayName: String,
      val cost: UiMoney,
      val contractGroup: ContractGroup,
    )

    data class ExistingDiscount(
      val campaignCode: CampaignCode,
      val displayName: String,
    )

    sealed interface CampaignsStatus {
      data class AddNewCampaignCode(
        val discountCodeInput: String,
        val discountError: DiscountError?,
        val isSubmittingNewCampaignCode: Boolean,
      ) : CampaignsStatus {
        val canSubmit: Boolean
          get() = discountCodeInput.isNotBlank()

        data class DiscountError(
          val errorMessage: ErrorMessage?,
        )
      }

      data class ExistingDiscounts(
        val activeDiscounts: List<ExistingDiscount>,
      ) : CampaignsStatus
    }

    sealed interface PaymentDetails {
      data class PaymentConnected(
        val displayName: String,
        val censoredPaymentDetails: String,
      ) : PaymentDetails

      data object PaymentConnectionPending : PaymentDetails

      data object NoPaymentConnected : PaymentDetails

      data object Unknown : PaymentDetails
    }
  }
}

private fun PaymentData.toUiState(): PaymentUiState.Content {
  return PaymentUiState.Content(
    nextChargeStatus = if (upcomingChargeNet != null && upcomingChargeDate != null) {
      PaymentUiState.Content.NextChargeStatus.UpcomingCharge(
        nextCharge = upcomingChargeNet,
        nextChargeDate = upcomingChargeDate,
      )
    } else {
      PaymentUiState.Content.NextChargeStatus.NoUpcomingCharge(monthlyCostNet)
    },
    netMonthlyCost = monthlyCostNet,
    monthlyCostDiscount = monthlyCostDiscount,
    insuranceCosts = agreements.map { agreement ->
      PaymentUiState.Content.InsuranceCost(
        displayName = agreement.displayName,
        cost = agreement.grossCost,
        contractGroup = agreement.contractGroup,
      )
    }.toImmutableList(),
    campaignsStatus = if (redeemedCampaigns.isNotEmpty()) {
      PaymentUiState.Content.CampaignsStatus.ExistingDiscounts(
        activeDiscounts = redeemedCampaigns.map { redeemedCampaign ->
          PaymentUiState.Content.ExistingDiscount(
            campaignCode = redeemedCampaign.campaignCode,
            displayName = redeemedCampaign.displayName,
          )
        },
      )
    } else {
      PaymentUiState.Content.CampaignsStatus.AddNewCampaignCode(
        discountCodeInput = "",
        discountError = null,
        isSubmittingNewCampaignCode = false,
      )
    },
    paymentDetails = when (paymentConnectionStatus) {
      PaymentData.PaymentConnectionStatus.ACTIVE -> {
        if (paymentDisplayName == null || paymentDescriptor == null) {
          PaymentUiState.Content.PaymentDetails.Unknown
        } else {
          PaymentUiState.Content.PaymentDetails.PaymentConnected(
            displayName = paymentDisplayName,
            censoredPaymentDetails = paymentDescriptor,
          )
        }
      }

      PaymentData.PaymentConnectionStatus.PENDING -> PaymentUiState.Content.PaymentDetails.PaymentConnectionPending
      PaymentData.PaymentConnectionStatus.NEEDS_SETUP -> PaymentUiState.Content.PaymentDetails.NoPaymentConnected
      PaymentData.PaymentConnectionStatus.UNKNOWN -> PaymentUiState.Content.PaymentDetails.Unknown
    },
  )
}
