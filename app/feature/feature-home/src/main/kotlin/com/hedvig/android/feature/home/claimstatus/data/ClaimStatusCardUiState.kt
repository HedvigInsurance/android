package com.hedvig.android.feature.home.claimstatus.data

import com.hedvig.android.feature.home.claimstatus.claimprogress.ClaimProgressUiState
import giraffe.HomeQuery

internal data class ClaimStatusCardUiState(
  val id: String,
  val pillsUiState: List<PillUiState>,
  val title: String,
  val subtitle: String,
  val claimProgressItemsUiState: List<ClaimProgressUiState>,
) {
  companion object {
    fun fromClaimStatusCardsQuery(
      claimStatusCard: HomeQuery.ClaimStatusCard,
    ): ClaimStatusCardUiState {
      return ClaimStatusCardUiState(
        id = claimStatusCard.id,
        pillsUiState = PillUiState.fromClaimStatusCardsQuery(claimStatusCard),
        title = claimStatusCard.title,
        subtitle = claimStatusCard.subtitle,
        claimProgressItemsUiState = ClaimProgressUiState.fromProgressSegments(
          claimStatusCard.progressSegments.map { it.fragments.progressSegments },
        ),
      )
    }
  }
}
