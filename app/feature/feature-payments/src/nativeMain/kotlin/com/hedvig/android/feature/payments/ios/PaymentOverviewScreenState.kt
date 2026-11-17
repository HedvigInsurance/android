package com.hedvig.android.feature.payments.ios

/**
 * The payments overview. Which cards, notices and rows it shows is each platform's own rule, read from these values
 * (for example [Content.memberType] for the discounts, history and payment methods rows).
 */
sealed class PaymentOverviewScreenState {
  data object Loading : PaymentOverviewScreenState()

  data object Failed : PaymentOverviewScreenState()

  /**
   * [upcomingCharge] is null when the member has no upcoming charge. [ongoingCharges] are charges still being
   * processed, in the order the backend lists them. [showsRetryChargeNotice] is the backend's flag that a failed
   * charge will be tried again. [anyPayinMethodIsPending] is true while any of the member's payin methods is still
   * being set up.
   */
  data class Content(
    val upcomingCharge: PaymentOverviewCharge?,
    val upcomingChargeNotice: PaymentUpcomingChargeNotice,
    val ongoingCharges: List<PaymentOverviewCharge>,
    val connectionStatus: PaymentConnectionStatus,
    val primaryPayinMethod: PaymentPrimaryPayinMethod?,
    val memberType: PaymentMemberType,
    val showsRetryChargeNotice: Boolean,
    val anyPayinMethodIsPending: Boolean,
  ) : PaymentOverviewScreenState()

  /** Calls the closure for this state's case, so a Swift call site stops compiling when a case is added. */
  fun handle(onLoading: () -> Unit, onFailed: () -> Unit, onContent: (Content) -> Unit) {
    when (this) {
      Loading -> onLoading()
      Failed -> onFailed()
      is Content -> onContent(this)
    }
  }
}

/** [dueDate] is `yyyy-MM-dd`. A null [id] is a charge the backend sent without one. */
data class PaymentOverviewCharge(
  val id: String?,
  val dueDate: String,
  val netAmount: PaymentMoney,
)

/** What the overview says about the upcoming charge, beside its amount. */
sealed class PaymentUpcomingChargeNotice {
  data object NoNotice : PaymentUpcomingChargeNotice()

  /** The upcoming charge is being processed. */
  data object InProgress : PaymentUpcomingChargeNotice()

  /**
   * The charge for [failedPeriodFromDate] to [failedPeriodToDate] (`yyyy-MM-dd`) failed. [manualChargeAmountDue] is
   * set when the member can pay the missed amount now, through manual charge.
   */
  data class ChargeFailed(
    val failedPeriodFromDate: String,
    val failedPeriodToDate: String,
    val manualChargeAmountDue: PaymentMoney?,
  ) : PaymentUpcomingChargeNotice()

  /** Calls the closure for this case, so a Swift call site stops compiling when a case is added. */
  fun handle(onNoNotice: () -> Unit, onInProgress: () -> Unit, onChargeFailed: (ChargeFailed) -> Unit) {
    when (this) {
      NoNotice -> onNoNotice()
      InProgress -> onInProgress()
      is ChargeFailed -> onChargeFailed(this)
    }
  }
}

/** Which of the member's payment connections, if any, the backend still asks for. */
sealed class PaymentConnectionStatus {
  data object Active : PaymentConnectionStatus()

  /** The member's default payin method is still being activated. */
  data object Pending : PaymentConnectionStatus()

  /**
   * No payin method is connected. [dueDateToConnect] (`yyyy-MM-dd`) is the earliest date a contract ends for missed
   * payments unless one is connected, null when none is ending.
   */
  data class NeedsPayinSetup(val dueDateToConnect: String?) : PaymentConnectionStatus()

  data object NeedsPayoutSetup : PaymentConnectionStatus()

  data object Unknown : PaymentConnectionStatus()

  /** Calls the closure for this case, so a Swift call site stops compiling when a case is added. */
  fun handle(
    onActive: () -> Unit,
    onPending: () -> Unit,
    onNeedsPayinSetup: (NeedsPayinSetup) -> Unit,
    onNeedsPayoutSetup: () -> Unit,
    onUnknown: () -> Unit,
  ) {
    when (this) {
      Active -> onActive()
      Pending -> onPending()
      is NeedsPayinSetup -> onNeedsPayinSetup(this)
      NeedsPayoutSetup -> onNeedsPayoutSetup()
      Unknown -> onUnknown()
    }
  }
}

/**
 * The method the member is charged with. [method] is never [PaymentChargeMethod.UNKNOWN] here. A [isPending] method
 * is still being set up.
 */
data class PaymentPrimaryPayinMethod(
  val method: PaymentChargeMethod,
  val isPending: Boolean,
)

enum class PaymentMemberType {
  QASA_ONLY,
  STANDARD,
  STANDARD_TO_QASA,
  ;

  /** Calls the closure for this entry, so a Swift call site stops compiling when an entry is added. */
  fun handle(onQasaOnly: () -> Unit, onStandard: () -> Unit, onStandardToQasa: () -> Unit) {
    when (this) {
      QASA_ONLY -> onQasaOnly()
      STANDARD -> onStandard()
      STANDARD_TO_QASA -> onStandardToQasa()
    }
  }
}
