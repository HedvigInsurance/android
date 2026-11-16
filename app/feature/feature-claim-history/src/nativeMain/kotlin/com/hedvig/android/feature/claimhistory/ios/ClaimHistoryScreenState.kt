package com.hedvig.android.feature.claimhistory.ios

import platform.Foundation.NSDate

sealed class ClaimHistoryScreenState {
  data object Loading : ClaimHistoryScreenState()

  data object Empty : ClaimHistoryScreenState()

  data object Failed : ClaimHistoryScreenState()

  data class Content(val items: List<ClaimHistoryItem>) : ClaimHistoryScreenState()
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
}
