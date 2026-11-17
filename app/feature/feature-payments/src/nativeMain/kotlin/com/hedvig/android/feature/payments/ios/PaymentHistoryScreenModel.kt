@file:OptIn(ExperimentalObjCRefinement::class)

package com.hedvig.android.feature.payments.ios

import androidx.lifecycle.ViewModelProvider
import com.hedvig.android.feature.payments.data.PaymentHistoryItem
import com.hedvig.android.feature.payments.ui.history.PaymentHistoryEvent
import com.hedvig.android.feature.payments.ui.history.PaymentHistoryUiState
import com.hedvig.android.feature.payments.ui.history.PaymentHistoryViewModel
import com.hedvig.android.molecule.public.ScreenModel
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
fun createPaymentHistoryScreenModel(viewModelFactory: ViewModelProvider.Factory): PaymentHistoryScreenModel =
  PaymentHistoryScreenModel(viewModelFactory)

/** The payment history screen. The Swift owner's rules are on [ScreenModel]. */
class PaymentHistoryScreenModel internal constructor(viewModelFactory: ViewModelProvider.Factory) :
  ScreenModel<PaymentHistoryScreenState>() {
    private val viewModel = viewModel(viewModelFactory, PaymentHistoryViewModel::class) { it.toScreenState() }

    /** Loads the history again. Safe to call from any thread. */
    fun reload() {
      viewModel.send(PaymentHistoryEvent.Reload)
    }
  }

/** The Android screen shows the older charges notice above this many charges. */
private const val OLDER_CHARGES_NOTICE_THRESHOLD = 11

internal fun PaymentHistoryUiState.toScreenState(): PaymentHistoryScreenState = when (this) {
  PaymentHistoryUiState.Loading -> PaymentHistoryScreenState.Loading

  PaymentHistoryUiState.Failure -> PaymentHistoryScreenState.Failed

  is PaymentHistoryUiState.Success -> if (paymentHistory.isEmpty()) {
    PaymentHistoryScreenState.Empty
  } else {
    PaymentHistoryScreenState.Content(
      charges = paymentHistory.map { it.toCharge() },
      showsOlderChargesNotice = paymentHistory.size > OLDER_CHARGES_NOTICE_THRESHOLD,
    )
  }
}

private fun PaymentHistoryItem.toCharge(): PaymentHistoryCharge = PaymentHistoryCharge(
  id = id.toChargeIdOrNull(),
  dueDate = dueDate.toIsoDateString(),
  netAmount = netAmount.toPaymentMoney(),
  status = status.toPaymentChargeStatus(),
)
