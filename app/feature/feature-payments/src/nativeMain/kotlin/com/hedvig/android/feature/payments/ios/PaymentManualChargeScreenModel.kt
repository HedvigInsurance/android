@file:OptIn(ExperimentalObjCRefinement::class)

package com.hedvig.android.feature.payments.ios

import androidx.lifecycle.ViewModelProvider
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.feature.payments.data.ManualChargeInfo
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeEvent
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeUiState
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeViewModel
import com.hedvig.android.molecule.public.ScreenModel
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
fun createPaymentManualChargeScreenModel(viewModelFactory: ViewModelProvider.Factory): PaymentManualChargeScreenModel =
  PaymentManualChargeScreenModel(viewModelFactory)

/**
 * Paying a missed charge. Creating, observing or closing the model never charges: only [triggerCharge] does. The Swift
 * owner's rules are on [ScreenModel].
 */
class PaymentManualChargeScreenModel internal constructor(viewModelFactory: ViewModelProvider.Factory) :
  ScreenModel<PaymentManualChargeScreenState>() {
    private val viewModel = viewModel(viewModelFactory, ManualChargeViewModel::class) { it.toScreenState() }

    /** Loads the missed charge again, through Loading. Safe to call from any thread. */
    fun retry() {
      viewModel.send(ManualChargeEvent.Retry)
    }

    /**
     * Loads the missed charge again while keeping the shown state, as when the screen shows again after the member
     * changed their payin method elsewhere. A failure to load is ignored. Safe to call from any thread.
     */
    fun refresh() {
      viewModel.send(ManualChargeEvent.Refresh)
    }

    /**
     * Charges the member the amount due, on their primary payin method. This moves money. Ignored unless the state is
     * Content. Safe to call from any thread.
     */
    fun triggerCharge() {
      viewModel.send(ManualChargeEvent.TriggerCharge)
    }
  }

internal fun ManualChargeUiState.toScreenState(): PaymentManualChargeScreenState = when (this) {
  ManualChargeUiState.Loading -> PaymentManualChargeScreenState.Loading

  ManualChargeUiState.NoLongerChargeable -> PaymentManualChargeScreenState.NoLongerChargeable

  is ManualChargeUiState.Failure -> PaymentManualChargeScreenState.Failed(error.message)

  is ManualChargeUiState.Success -> manualChargeInfo.toContent(isCharging = payButtonLoading)

  is ManualChargeUiState.Charged -> PaymentManualChargeScreenState.Charged(
    showsCancellationWarning = manualChargeInfo.showCancellationWarning,
    primaryPayinMethod = manualChargeInfo.primaryPayinMethod?.toPaymentChargeMethod(),
  )
}

private fun ManualChargeInfo.toContent(isCharging: Boolean): PaymentManualChargeScreenState.Content =
  PaymentManualChargeScreenState.Content(
    chargeId = chargeId.toChargeIdOrNull(),
    missedDueDate = missedDueDate.toIsoDateString(),
    amountDue = amountDue.toPaymentMoney(),
    payinMethods = currentMethods.map {
      PaymentConnectedPayinAccount(it.toPaymentPayinAccount(), isPending = it.isPending, isDefault = it.isDefault)
    },
    primaryPayinAccount = primaryPayinMethod?.toPaymentPayinAccount(),
    showsCancellationWarning = showCancellationWarning,
    isCharging = isCharging,
  )

private fun PayinAccount.toPaymentChargeMethod(): PaymentChargeMethod = when (this) {
  is PayinAccount.Trustly -> PaymentChargeMethod.TRUSTLY
  is PayinAccount.SwishPayin -> PaymentChargeMethod.SWISH
  is PayinAccount.Invoice -> PaymentChargeMethod.INVOICE
}
