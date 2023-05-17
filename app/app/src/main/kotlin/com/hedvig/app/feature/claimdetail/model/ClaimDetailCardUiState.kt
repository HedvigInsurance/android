package com.hedvig.app.feature.claimdetail.model

import com.hedvig.app.ui.compose.composables.claimprogress.ClaimProgressUiState
import giraffe.ClaimDetailsQuery

data class ClaimDetailCardUiState(
  val claimProgressItemsUiState: List<ClaimProgressUiState>,
  val statusParagraph: String,
) {

  companion object {
    fun fromDto(dto: ClaimDetailsQuery.ClaimDetail): ClaimDetailCardUiState {
      return ClaimDetailCardUiState(
        claimProgressItemsUiState = ClaimProgressUiState.fromProgressSegments(
          dto.progressSegments.map { it.fragments.progressSegments },
        ),
        statusParagraph = dto.claim.statusParagraph,
      )
    }
  }
}
