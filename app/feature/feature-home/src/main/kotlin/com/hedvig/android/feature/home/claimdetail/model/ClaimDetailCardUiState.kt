package com.hedvig.android.feature.home.claimdetail.model

import com.hedvig.android.feature.home.claimstatus.claimprogress.ClaimProgressUiState
import giraffe.ClaimDetailsQuery

internal data class ClaimDetailCardUiState(
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
