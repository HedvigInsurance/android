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
 * Paying a missed charge. Creating, observing or closing the model never charges: only [triggerCharge] does. Once the
 * state is Charged, every event function does nothing. The Swift owner's rules are on [ScreenModel].
 */
class PaymentManualChargeScreenModel internal constructor(viewModelFactory: ViewModelProvider.Factory) :
  ScreenModel<PaymentManualChargeScreenState>() {
    private val viewModel = viewModel(viewModelFactory, ManualChargeViewModel::class) { it.toScreenState() }

    /** Loads the missed charge again, through Loading. Safe to call from any thread. */
    fun retry() {
      if (currentState.acceptsReload()) viewModel.send(ManualChargeEvent.Retry)
    }

    /**
     * Loads the missed charge again while keeping the shown state, as when the screen shows again after the member
     * changed their payin method elsewhere. A failure to load is ignored. Safe to call from any thread.
     */
    fun refresh() {
      if (currentState.acceptsReload()) viewModel.send(ManualChargeEvent.Refresh)
    }

    /**
     * Charges the member the amount due, on their primary payin method. This moves money. It does nothing unless the
     * state is Content with `isCharging` false, so a charge in flight is never started again.
     *
     * Call it only while [states] is being collected, and keep collecting for as long as `isCharging` is true: the
     * presenter stops 5 seconds after the last collection ends, which cancels the charge's request, and the Charged
     * state would then never arrive although the member may have been charged. Safe to call from any thread.
     */
    fun triggerCharge() {
      currentState.triggerChargeEvent()?.let { viewModel.send(it) }
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

/** The event [PaymentManualChargeScreenModel.triggerCharge] sends from this state, null when no charge may start. */
internal fun PaymentManualChargeScreenState.triggerChargeEvent(): ManualChargeEvent? =
  if (this is PaymentManualChargeScreenState.Content && !isCharging) ManualChargeEvent.TriggerCharge else null

/** Whether retry and refresh reach the ViewModel from this state. */
internal fun PaymentManualChargeScreenState.acceptsReload(): Boolean = this !is PaymentManualChargeScreenState.Charged
