@file:OptIn(ExperimentalObjCRefinement::class)

package com.hedvig.android.feature.payments.ios

import androidx.lifecycle.ViewModelProvider
import com.hedvig.android.data.paying.member.MemberType
import com.hedvig.android.feature.payin.account.navigation.PayinMethodId
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.feature.payments.data.PrimaryPayinMethod
import com.hedvig.android.feature.payments.ui.payments.PaymentsEvent
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.ConnectedPaymentInfo
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.UpcomingPayment
import com.hedvig.android.feature.payments.ui.payments.PaymentsUiState.Content.UpcomingPaymentInfo
import com.hedvig.android.feature.payments.ui.payments.PaymentsViewModel
import com.hedvig.android.molecule.public.ScreenModel
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@HiddenFromObjC
fun createPaymentOverviewScreenModel(viewModelFactory: ViewModelProvider.Factory): PaymentOverviewScreenModel =
  PaymentOverviewScreenModel(viewModelFactory)

/** The payments overview. The Swift owner's rules are on [ScreenModel]. */
class PaymentOverviewScreenModel internal constructor(viewModelFactory: ViewModelProvider.Factory) :
  ScreenModel<PaymentOverviewScreenState>() {
    private val viewModel = viewModel(viewModelFactory, PaymentsViewModel::class) { it.toScreenState() }

    /** Loads the overview again. Safe to call from any thread. */
    fun retry() {
      viewModel.send(PaymentsEvent.Retry)
    }
  }

internal fun PaymentsUiState.toScreenState(): PaymentOverviewScreenState = when (this) {
  PaymentsUiState.Loading -> PaymentOverviewScreenState.Loading

  PaymentsUiState.Error -> PaymentOverviewScreenState.Failed

  is PaymentsUiState.Content -> PaymentOverviewScreenState.Content(
    upcomingCharge = when (val payment = upcomingPayment) {
      UpcomingPayment.NoUpcomingPayment -> null

      is UpcomingPayment.Content -> PaymentOverviewCharge(
        id = payment.id.toChargeIdOrNull(),
        dueDate = payment.dueDate.toIsoDateString(),
        netAmount = payment.netAmount.toPaymentMoney(),
      )
    },
    upcomingChargeNotice = upcomingPaymentInfo.toNotice(),
    ongoingCharges = ongoingCharges.map { it.toCharge() },
    connectionStatus = connectedPaymentInfo.toConnectionStatus(),
    primaryPayinMethod = primaryPayinMethod?.toPrimaryPayinMethod(),
    memberType = memberType.toPaymentMemberType(),
    showsRetryChargeNotice = showRetryChargeNotice,
    anyPayinMethodIsPending = anyPayinMethodIsPending,
  )
}

private fun UpcomingPaymentInfo.toNotice(): PaymentUpcomingChargeNotice = when (this) {
  UpcomingPaymentInfo.NoInfo -> PaymentUpcomingChargeNotice.NoNotice

  UpcomingPaymentInfo.InProgress -> PaymentUpcomingChargeNotice.InProgress

  is UpcomingPaymentInfo.PaymentFailed -> PaymentUpcomingChargeNotice.ChargeFailed(
    failedPeriodFromDate = failedPaymentStartDate.toIsoDateString(),
    failedPeriodToDate = failedPaymentEndDate.toIsoDateString(),
    manualChargeAmountDue = isManualChargeAllowed?.sum?.toPaymentMoney(),
  )
}

private fun PaymentOverview.OngoingCharge.toCharge(): PaymentOverviewCharge = PaymentOverviewCharge(
  id = id.toChargeIdOrNull(),
  dueDate = date.toIsoDateString(),
  netAmount = netAmount.toPaymentMoney(),
)

private fun ConnectedPaymentInfo.toConnectionStatus(): PaymentConnectionStatus = when (this) {
  ConnectedPaymentInfo.Active -> PaymentConnectionStatus.Active

  ConnectedPaymentInfo.Pending -> PaymentConnectionStatus.Pending

  is ConnectedPaymentInfo.NeedsPayinSetup -> PaymentConnectionStatus.NeedsPayinSetup(
    dueDateToConnect?.toIsoDateString(),
  )

  ConnectedPaymentInfo.NeedsPayoutSetup -> PaymentConnectionStatus.NeedsPayoutSetup

  ConnectedPaymentInfo.Unknown -> PaymentConnectionStatus.Unknown
}

private fun PrimaryPayinMethod.toPrimaryPayinMethod(): PaymentPrimaryPayinMethod = PaymentPrimaryPayinMethod(
  method = when (id) {
    PayinMethodId.Trustly -> PaymentChargeMethod.TRUSTLY
    PayinMethodId.Swish -> PaymentChargeMethod.SWISH
    PayinMethodId.Invoice -> PaymentChargeMethod.INVOICE
  },
  isPending = isPending,
)

private fun MemberType.toPaymentMemberType(): PaymentMemberType = when (this) {
  MemberType.QASA_ONLY_MEMBER -> PaymentMemberType.QASA_ONLY
  MemberType.STANDARD_MEMBER -> PaymentMemberType.STANDARD
  MemberType.STANDARD_TO_QASA_MEMBER -> PaymentMemberType.STANDARD_TO_QASA
}
