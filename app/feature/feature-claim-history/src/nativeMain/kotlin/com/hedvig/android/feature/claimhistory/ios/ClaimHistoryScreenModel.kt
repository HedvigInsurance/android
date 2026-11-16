@file:OptIn(ExperimentalObjCRefinement::class)

package com.hedvig.android.feature.claimhistory.ios

import androidx.lifecycle.ViewModelProvider
import com.hedvig.android.feature.claimhistory.ClaimHistory
import com.hedvig.android.feature.claimhistory.ClaimHistoryEvent
import com.hedvig.android.feature.claimhistory.ClaimHistoryUiState
import com.hedvig.android.feature.claimhistory.ClaimHistoryViewModel
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.ScreenModel
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlin.time.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSDate

@HiddenFromObjC
fun createClaimHistoryScreenModel(viewModelFactory: ViewModelProvider.Factory): ClaimHistoryScreenModel =
  ClaimHistoryScreenModel(viewModelFactory)

/** The claim history screen. The Swift owner's rules are on [ScreenModel]. */
class ClaimHistoryScreenModel internal constructor(viewModelFactory: ViewModelProvider.Factory) :
  ScreenModel<ClaimHistoryScreenState>() {
    private val viewModel = viewModel(viewModelFactory, ClaimHistoryViewModel::class) { it.toScreenState() }

    /** Loads the history again. Safe to call from any thread. */
    fun reload() {
      viewModel.send(ClaimHistoryEvent.Reload)
    }
  }

private fun ClaimHistoryUiState.toScreenState(): ClaimHistoryScreenState = when (this) {
  ClaimHistoryUiState.Loading -> ClaimHistoryScreenState.Loading
  ClaimHistoryUiState.NoHistory -> ClaimHistoryScreenState.Empty
  ClaimHistoryUiState.Error -> ClaimHistoryScreenState.Failed
  is ClaimHistoryUiState.Content -> ClaimHistoryScreenState.Content(claims.toList().map { it.toItem() })
}

private fun ClaimHistory.toItem(): ClaimHistoryItem = ClaimHistoryItem(
  id = id,
  claimType = claimType,
  submittedAt = submittedAt?.toNSDateOrNull(),
  outcome = when (outcome) {
    ClaimHistory.ClaimOutcome.PAID -> ClaimHistoryOutcome.PAID
    ClaimHistory.ClaimOutcome.NOT_COMPENSATED -> ClaimHistoryOutcome.NOT_COMPENSATED
    ClaimHistory.ClaimOutcome.NOT_COVERED -> ClaimHistoryOutcome.NOT_COVERED
    ClaimHistory.ClaimOutcome.UNRESPONSIVE -> ClaimHistoryOutcome.UNRESPONSIVE
    ClaimHistory.ClaimOutcome.UNKNOWN, null -> ClaimHistoryOutcome.UNKNOWN
  },
  isPartnerClaim = isPartnerClaim,
)

private fun Instant.toNSDateOrNull(): NSDate? = try {
  toNSDate()
} catch (e: IllegalArgumentException) {
  logcat(LogPriority.WARN, e) { "Claim submission time $this is outside the range NSDate holds" }
  null
}
