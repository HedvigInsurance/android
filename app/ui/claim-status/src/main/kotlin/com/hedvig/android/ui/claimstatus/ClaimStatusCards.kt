package com.hedvig.android.ui.claimstatus

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import com.hedvig.android.compose.pager.indicator.CardCarousel
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.ui.claimstatus.model.ClaimCardUiState
import com.hedvig.android.ui.claimstatus.model.ClaimPillType.Claim
import com.hedvig.android.ui.claimstatus.model.ClaimPillType.Closed.NotCompensated
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentText.Closed
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment.SegmentType
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import kotlin.time.Instant

@Composable
fun ClaimStatusCards(
  onClick: (claimId: String) -> Unit,
  onContinueDraftClaim: () -> Unit,
  onDeleteDraftClaim: (draftId: String) -> Unit,
  claimCardsUiState: NonEmptyList<ClaimCardUiState>,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  CardCarousel(
    items = claimCardsUiState,
    contentPadding = contentPadding,
    modifier = modifier,
  ) { card, cardModifier ->
    ClaimCard(
      uiState = card,
      onClick = onClick,
      onContinueDraftClaim = onContinueDraftClaim,
      onDeleteDraftClaim = onDeleteDraftClaim,
      modifier = cardModifier,
    )
  }
}

@Composable
private fun ClaimCard(
  uiState: ClaimCardUiState,
  onClick: (claimId: String) -> Unit,
  onContinueDraftClaim: () -> Unit,
  onDeleteDraftClaim: (draftId: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  when (uiState) {
    is ClaimCardUiState.Claim -> ClaimStatusCard(
      uiState = uiState.uiState,
      onClick = onClick,
      modifier = modifier,
    )

    is ClaimCardUiState.Draft -> DraftClaimCard(
      uiState = uiState,
      onContinueClick = onContinueDraftClaim,
      onDeleteClick = { onDeleteDraftClaim(uiState.id) },
      modifier = modifier,
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimStatusCards() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ClaimStatusCards(
        onClick = {},
        onContinueDraftClaim = {},
        onDeleteDraftClaim = {},
        contentPadding = PaddingValues(horizontal = 16.dp),
        claimCardsUiState = listOf(
          ClaimCardUiState.Draft("id", "My things", Instant.parse("2026-07-02T00:00:00Z")),
          ClaimCardUiState.Claim(
            ClaimStatusCardUiState(
              id = "id#0",
              pillTypes = listOf(Claim, NotCompensated),
              claimProgressItemsUiState = listOf(
                ClaimProgressSegment(Closed, SegmentType.INACTIVE),
              ),
              claimType = "Broken item",
              insuranceDisplayName = "Home Insurance Homeowner",
              submittedDate = Instant.parse("2024-05-01T00:00:00Z"),
            ),
          ),
          ClaimCardUiState.Claim(
            ClaimStatusCardUiState(
              id = "id#1",
              pillTypes = listOf(Claim, NotCompensated),
              claimProgressItemsUiState = listOf(
                ClaimProgressSegment(Closed, SegmentType.INACTIVE),
              ),
              claimType = "Broken item",
              insuranceDisplayName = "Home Insurance Homeowner",
              submittedDate = Instant.parse("2024-05-01T00:00:00Z"),
            ),
          ),
        ).toNonEmptyListOrNull()!!,
      )
    }
  }
}
