package com.hedvig.android.feature.odyssey.step.singleitemcheckout

import androidx.lifecycle.ViewModel
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import com.hedvig.android.data.claimflow.CheckoutMethod
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class SingleItemCheckoutViewModel(
  singleItemCheckout: ClaimFlowDestination.SingleItemCheckout,
) : ViewModel() {
  private val _uiState: MutableStateFlow<SingleItemCheckoutUiState> =
    MutableStateFlow(SingleItemCheckoutUiState.fromSingleItemCheckout(singleItemCheckout))
  val uiState: StateFlow<SingleItemCheckoutUiState> = _uiState.asStateFlow()

  fun selectCheckoutMethod(secondCheckoutMethod: CheckoutMethod.Known) {
    _uiState.update {
      it.asContent()?.copy(selectedCheckoutMethod = secondCheckoutMethod) ?: it
    }
  }
}

internal sealed interface SingleItemCheckoutUiState {
  fun asContent(): Content? = this as? Content

  data class Content(
    val compensation: ClaimFlowDestination.SingleItemCheckout.Compensation.Known,
    val availableCheckoutMethods: NonEmptyList<CheckoutMethod.Known>,
    val selectedCheckoutMethod: CheckoutMethod.Known,
    val modelDisplayName: String,
  ) : SingleItemCheckoutUiState

  /**
   * When we do not get any known checkout methods, we need to directly ask the member to update the app, as this screen
   * can't work without a valid checkout method.
   * Same with if we don't get any known Compensation (value or repair).
   */
  object Unavailable : SingleItemCheckoutUiState

  companion object {
    fun fromSingleItemCheckout(
      singleItemCheckout: ClaimFlowDestination.SingleItemCheckout,
    ): SingleItemCheckoutUiState {
      val availableCheckoutMethods = singleItemCheckout.availableCheckoutMethods.toNonEmptyListOrNull()
      val initiallySelectedCheckoutMethod =
        availableCheckoutMethods?.firstOrNull() ?: return Unavailable
      val checkedCompensation = singleItemCheckout.compensation as? ClaimFlowDestination.SingleItemCheckout.Compensation.Known
        ?: return Unavailable
      val modelName = singleItemCheckout.modelName ?: singleItemCheckout.customName
        ?: singleItemCheckout.brandName ?: "-"
      return Content(
        compensation = checkedCompensation,
        availableCheckoutMethods = availableCheckoutMethods,
        selectedCheckoutMethod = initiallySelectedCheckoutMethod,
        modelDisplayName = modelName,
      )
    }
  }
}
