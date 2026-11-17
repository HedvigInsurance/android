package com.hedvig.android.feature.payments.ios

/** Paying a missed charge now, on the member's primary payin method. */
sealed class PaymentManualChargeScreenState {
  data object Loading : PaymentManualChargeScreenState()

  /** The backend no longer offers the missed charge for the member to pay here. */
  data object NoLongerChargeable : PaymentManualChargeScreenState()

  /** [userErrorMessage] is the backend's text when it refused the charge, null for any other failure. */
  data class Failed(val userErrorMessage: String?) : PaymentManualChargeScreenState()

  /**
   * The missed charge, due on [missedDueDate] (`yyyy-MM-dd`). A null [chargeId] is a charge the backend sent without
   * one. [payinMethods] are the member's payin methods the app can show; [primaryPayinAccount] is the one charged,
   * null when none is connected. [showsCancellationWarning] is true when a contract ends for missed payments.
   * [isCharging] is true while the charge is being made.
   */
  data class Content(
    val chargeId: String?,
    val missedDueDate: String,
    val amountDue: PaymentMoney,
    val payinMethods: List<PaymentConnectedPayinAccount>,
    val primaryPayinAccount: PaymentPayinAccount?,
    val showsCancellationWarning: Boolean,
    val isCharging: Boolean,
  ) : PaymentManualChargeScreenState()

  /**
   * The charge went through. Nothing follows this state: the platform leaves the screen and shows its success with
   * [showsCancellationWarning] and [primaryPayinMethod], the method charged.
   */
  data class Charged(
    val showsCancellationWarning: Boolean,
    val primaryPayinMethod: PaymentChargeMethod?,
  ) : PaymentManualChargeScreenState()

  /** Calls the closure for this state's case, so a Swift call site stops compiling when a case is added. */
  fun handle(
    onLoading: () -> Unit,
    onNoLongerChargeable: () -> Unit,
    onFailed: (Failed) -> Unit,
    onContent: (Content) -> Unit,
    onCharged: (Charged) -> Unit,
  ) {
    when (this) {
      Loading -> onLoading()
      NoLongerChargeable -> onNoLongerChargeable()
      is Failed -> onFailed(this)
      is Content -> onContent(this)
      is Charged -> onCharged(this)
    }
  }
}

/** One of the member's payin methods. A [isPending] method is still being set up. */
data class PaymentConnectedPayinAccount(
  val account: PaymentPayinAccount,
  val isPending: Boolean,
  val isDefault: Boolean,
)
