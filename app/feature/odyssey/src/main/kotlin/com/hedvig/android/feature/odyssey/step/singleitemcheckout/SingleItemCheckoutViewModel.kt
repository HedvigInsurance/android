package com.hedvig.android.feature.odyssey.step.singleitemcheckout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.claimflow.CheckoutMethod
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SingleItemCheckoutViewModel(
  singleItemCheckout: ClaimFlowDestination.SingleItemCheckout,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {

  private val _uiState: MutableStateFlow<SingleItemCheckoutUiState> =
    MutableStateFlow(SingleItemCheckoutUiState.fromSingleItemCheckout(singleItemCheckout))
  val uiState: StateFlow<SingleItemCheckoutUiState> = _uiState.asStateFlow()

  fun selectCheckoutMethod(secondCheckoutMethod: CheckoutMethod.Known) {
    _uiState.update {
      it.asContent()?.copy(selectedCheckoutMethod = secondCheckoutMethod) ?: it
    }
  }

  fun requestPayout() {
    val uiState = _uiState.value.asContent() ?: return
    if (uiState.canRequestPayout.not()) return
    _uiState.update { uiState.copy(payoutStatus = PayoutUiState.Status.Loading) }
    viewModelScope.launch {
      when (
        val submitResult = claimFlowRepository.submitSingleItemCheckout(uiState.selectedCheckoutMethod.uiMoney.amount)
      ) {
        is Either.Left -> {
          _uiState.update {
            uiState.copy(payoutStatus = PayoutUiState.Status.Error)
          }
        }
        is Either.Right -> {
          val claimFlowStep = submitResult.value
          _uiState.update {
            uiState.copy(
              payoutStatus = PayoutUiState.Status.PaidOut(claimFlowStep),
            )
          }
        }
      }
    }
  }
}

internal sealed interface SingleItemCheckoutUiState {
  fun asContent(): Content? = this as? Content

  data class Content(
    val price: UiMoney,
    val depreciation: UiMoney,
    val deductible: UiMoney,
    val payoutAmount: UiMoney,
    val availableCheckoutMethods: NonEmptyList<CheckoutMethod.Known>,
    val selectedCheckoutMethod: CheckoutMethod.Known,
    private val payoutStatus: PayoutUiState.Status = PayoutUiState.Status.NotStarted,
  ) : SingleItemCheckoutUiState {
    val canRequestPayout: Boolean = payoutStatus is PayoutUiState.Status.NotStarted
    val payoutUiState: PayoutUiState = PayoutUiState(selectedCheckoutMethod.uiMoney, payoutStatus)
  }

  /**
   * When we do not get any known checkout methods, we need to directly ask the member to update the app, as this screen
   * can't work without a valid checkout method
   */
  object Unavailable : SingleItemCheckoutUiState

  companion object {
    fun fromSingleItemCheckout(
      singleItemCheckout: ClaimFlowDestination.SingleItemCheckout,
    ): SingleItemCheckoutUiState {
      val availableCheckoutMethods = singleItemCheckout.availableCheckoutMethods.toNonEmptyListOrNull()
      val initiallySelectedCheckoutMethod =
        availableCheckoutMethods?.firstOrNull() ?: return Unavailable
      return Content(
        singleItemCheckout.price,
        singleItemCheckout.depreciation,
        singleItemCheckout.deductible,
        singleItemCheckout.payoutAmount,
        availableCheckoutMethods,
        initiallySelectedCheckoutMethod,
      )
    }
  }
}

internal data class PayoutUiState(
  val amount: UiMoney,
  val status: Status,
) {
  val shouldRender: Boolean
    get() = status != Status.NotStarted

  sealed interface Status {
    object NotStarted : Status // Before the member has started the payout process in the first place
    object Loading : Status // While the network request is being handled to give the payout
    object Error : Status // If an error has happened while processing the payout in the backend
    data class PaidOut(
      // Terminal state, where the payout is complete, and we can exit the flow
      val nextStep: ClaimFlowStep,
    ) : Status
  }
}
