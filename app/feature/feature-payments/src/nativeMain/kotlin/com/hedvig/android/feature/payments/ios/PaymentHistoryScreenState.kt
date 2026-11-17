package com.hedvig.android.feature.payments.ios

sealed class PaymentHistoryScreenState {
  data object Loading : PaymentHistoryScreenState()

  data object Empty : PaymentHistoryScreenState()

  data object Failed : PaymentHistoryScreenState()

  /**
   * [charges] in the order the backend lists them. [showsOlderChargesNotice] is true when the list is long enough
   * for the notice that older charges are not listed.
   */
  data class Content(
    val charges: List<PaymentHistoryCharge>,
    val showsOlderChargesNotice: Boolean,
  ) : PaymentHistoryScreenState()

  /** Calls the closure for this state's case, so a Swift call site stops compiling when a case is added. */
  fun handle(onLoading: () -> Unit, onEmpty: () -> Unit, onFailed: () -> Unit, onContent: (Content) -> Unit) {
    when (this) {
      Loading -> onLoading()
      Empty -> onEmpty()
      Failed -> onFailed()
      is Content -> onContent(this)
    }
  }
}

/** [dueDate] is `yyyy-MM-dd`. A null [id] is a charge the backend sent without one. */
data class PaymentHistoryCharge(
  val id: String?,
  val dueDate: String,
  val netAmount: PaymentMoney,
  val status: PaymentChargeStatus,
)
