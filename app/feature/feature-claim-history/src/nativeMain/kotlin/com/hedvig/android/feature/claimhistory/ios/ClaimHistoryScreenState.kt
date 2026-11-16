package com.hedvig.android.feature.claimhistory.ios

import platform.Foundation.NSDate

sealed class ClaimHistoryScreenState {
  data object Loading : ClaimHistoryScreenState()

  data object Empty : ClaimHistoryScreenState()

  data object Failed : ClaimHistoryScreenState()

  data class Content(val items: List<ClaimHistoryItem>) : ClaimHistoryScreenState()

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

class ClaimHistoryItem(
  val id: String,
  val claimType: String?,
  val submittedAt: NSDate?,
  val outcome: ClaimHistoryOutcome,
  val isPartnerClaim: Boolean,
)

enum class ClaimHistoryOutcome {
  PAID,
  NOT_COMPENSATED,
  NOT_COVERED,
  UNRESPONSIVE,
  UNKNOWN,
  ;

  /** Calls the closure for this entry, so a Swift call site stops compiling when an entry is added. */
  fun handle(
    onPaid: () -> Unit,
    onNotCompensated: () -> Unit,
    onNotCovered: () -> Unit,
    onUnresponsive: () -> Unit,
    onUnknown: () -> Unit,
  ) {
    when (this) {
      PAID -> onPaid()
      NOT_COMPENSATED -> onNotCompensated()
      NOT_COVERED -> onNotCovered()
      UNRESPONSIVE -> onUnresponsive()
      UNKNOWN -> onUnknown()
    }
  }
}
