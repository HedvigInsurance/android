package com.hedvig.android.ui.claimstatus.model

import kotlin.time.Instant
import octopus.fragment.ClaimFragment

data class ClaimStatusCardUiState(
  val id: String,
  val claimType: String?,
  val insuranceDisplayName: String?,
  val submittedDate: Instant,
  val pillTypes: List<ClaimPillType>,
  val claimProgressItemsUiState: List<ClaimProgressSegment>,
) {
  companion object {
    fun fromClaimStatusCardsQuery(claim: ClaimFragment): ClaimStatusCardUiState {
      return ClaimStatusCardUiState(
        id = claim.id,
        claimType = claim.claimType,
        insuranceDisplayName = claim.productVariant?.displayName,
        submittedDate = claim.submittedAt,
        pillTypes = ClaimPillType.fromClaimFragment(claim),
        claimProgressItemsUiState = ClaimProgressSegment.fromClaimFragment(claim),
      )
    }
  }
}
