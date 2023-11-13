package com.hedvig.android.feature.odyssey.step.singleitempayout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SingleItemPayoutViewModel(
  private val singleItemPayout: ClaimFlowDestination.SingleItemPayout,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {
  private val _uiState: MutableStateFlow<PayoutUiState> =
    MutableStateFlow(PayoutUiState(singleItemPayout.checkoutMethod.uiMoney, PayoutUiState.Status.NotStarted))
  val uiState: StateFlow<PayoutUiState> = _uiState.asStateFlow()

  init {
    requestPayout()
  }

  fun requestPayout() {
    if (uiState.value.canRequestPayout.not()) return
    _uiState.update { it.copy(status = PayoutUiState.Status.Loading) }
    viewModelScope.launch {
      when (val submitResult = claimFlowRepository.submitSingleItemCheckout(uiState.value.amount.amount)) {
        is Either.Left -> {
          logcat(LogPriority.ERROR, submitResult.value.throwable) {
            "SingleItemPayout request payout message:${submitResult.value.message}"
          }
          _uiState.update { it.copy(status = PayoutUiState.Status.Error) }
        }
        is Either.Right -> {
          _uiState.update { it.copy(status = PayoutUiState.Status.PaidOut) }
        }
      }
    }
  }
}

internal data class PayoutUiState(
  val amount: UiMoney,
  val status: Status,
) {
  val canRequestPayout: Boolean = status is PayoutUiState.Status.NotStarted || status is PayoutUiState.Status.Error

  sealed interface Status {
    object NotStarted : Status // Before the member has started the payout process in the first place

    object Loading : Status // While the network request is being handled to give the payout

    object Error : Status // If an error has happened while processing the payout in the backend

    object PaidOut : Status // Terminal state, where the payout is complete, and we can exit the flow
  }
}
