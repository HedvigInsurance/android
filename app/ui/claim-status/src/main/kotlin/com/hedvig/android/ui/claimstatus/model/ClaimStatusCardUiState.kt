package com.hedvig.android.ui.claimstatus.model

import octopus.fragment.ClaimFragment

data class ClaimStatusCardUiState(
  val id: String,
  val pillTypes: List<ClaimPillType>,
  val claimProgressItemsUiState: List<ClaimProgressSegment>,
) {
  companion object {
    fun fromClaimStatusCardsQuery(
      claim: ClaimFragment,
    ): ClaimStatusCardUiState {
      return ClaimStatusCardUiState(
        id = claim.id,
        pillTypes = ClaimPillType.fromClaimFragment(claim),
        claimProgressItemsUiState = ClaimProgressSegment.fromClaimFragment(claim),
      )
    }
  }
}
