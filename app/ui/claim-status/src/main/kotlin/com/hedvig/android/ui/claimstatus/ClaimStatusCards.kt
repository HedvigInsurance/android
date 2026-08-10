package com.hedvig.android.ui.claimstatus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import com.hedvig.android.compose.pager.indicator.HorizontalPagerIndicator
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LocalContentColor
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
  if (claimCardsUiState.size == 1) {
    ClaimCard(
      uiState = claimCardsUiState.first(),
      onClick = onClick,
      onContinueDraftClaim = onContinueDraftClaim,
      onDeleteDraftClaim = onDeleteDraftClaim,
      modifier = modifier.padding(contentPadding),
    )
  } else {
    val pagerState = rememberPagerState(pageCount = { claimCardsUiState.size })
    Column(modifier) {
      HorizontalPager(
        state = pagerState,
        contentPadding = contentPadding,
        beyondViewportPageCount = 1,
        pageSpacing = 8.dp,
        modifier = Modifier.fillMaxWidth().systemGestureExclusion(),
      ) { page: Int ->
        ClaimCard(
          uiState = claimCardsUiState[page],
          onClick = onClick,
          onContinueDraftClaim = onContinueDraftClaim,
          onDeleteDraftClaim = onDeleteDraftClaim,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      Spacer(Modifier.height(16.dp))
      HorizontalPagerIndicator(
        pagerState = pagerState,
        pageCount = claimCardsUiState.size,
        activeColor = LocalContentColor.current,
        modifier = Modifier.padding(contentPadding).align(Alignment.CenterHorizontally),
      )
    }
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
