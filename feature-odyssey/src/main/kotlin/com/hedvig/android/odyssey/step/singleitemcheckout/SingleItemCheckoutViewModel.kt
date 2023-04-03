package com.hedvig.android.odyssey.step.singleitemcheckout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import com.hedvig.android.odyssey.data.ClaimFlowRepository
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.navigation.CheckoutMethod
import com.hedvig.android.odyssey.navigation.ClaimFlowDestination
import com.hedvig.android.odyssey.navigation.UiGuaranteedMoney
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

  fun selectCheckoutMethod(secondCheckoutMethod: CheckoutMethod.Known.AutomaticAutogiro) {
    _uiState.update {
      it.asContent()?.copy(selectedCheckoutMethod = secondCheckoutMethod) ?: it
    }
  }

  fun requestPayout() {
    val uiState = _uiState.value.asContent() ?: return
    if (uiState.canSubmit.not()) return
    _uiState.update { uiState.copy(isLoading = true) }
    viewModelScope.launch {
      claimFlowRepository
        .submitSingleItemCheckout(uiState.selectedCheckoutMethod.uiMoney.amount)
        .fold(
          ifLeft = {
            _uiState.update { uiState.copy(isLoading = false, hasError = true) }
          },
          ifRight = { claimFlowStep ->
            _uiState.update { uiState.copy(isLoading = false, nextStep = claimFlowStep) }
          },
        )
    }
  }

  fun handledNextStepNavigation() {
    _uiState.update {
      it.asContent()?.copy(nextStep = null) ?: it
    }
  }

  fun showedError() {
    _uiState.update {
      it.asContent()?.copy(hasError = false) ?: it
    }
  }
}

internal sealed interface SingleItemCheckoutUiState {
  fun asContent(): Content? = this as? Content

  data class Content(
    val price: UiGuaranteedMoney,
    val depreciation: UiGuaranteedMoney,
    val deductible: UiGuaranteedMoney,
    val payoutAmount: UiGuaranteedMoney,
    val availableCheckoutMethods: NonEmptyList<CheckoutMethod.Known>,
    val selectedCheckoutMethod: CheckoutMethod.Known,
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val nextStep: ClaimFlowStep? = null,
  ) : SingleItemCheckoutUiState {
    val canSubmit: Boolean = !isLoading && !hasError && nextStep == null
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
