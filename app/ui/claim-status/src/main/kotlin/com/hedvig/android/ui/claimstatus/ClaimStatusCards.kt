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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import com.hedvig.android.compose.pager.indicator.HorizontalPagerIndicator
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.ui.claimstatus.model.ClaimPillType
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import kotlinx.datetime.Instant

@Composable
fun ClaimStatusCards(
  onClick: (claimId: String) -> Unit,
  claimStatusCardsUiState: NonEmptyList<ClaimStatusCardUiState>,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  if (claimStatusCardsUiState.size == 1) {
    ClaimStatusCard(
      uiState = claimStatusCardsUiState.first(),
      onClick = onClick,
      modifier = modifier.padding(contentPadding),
    )
  } else {
    val pagerState = rememberPagerState(pageCount = { claimStatusCardsUiState.size })
    Column(modifier) {
      HorizontalPager(
        state = pagerState,
        contentPadding = contentPadding,
        beyondViewportPageCount = 1,
        pageSpacing = 8.dp,
        modifier = Modifier.fillMaxWidth().systemGestureExclusion(),
      ) { page: Int ->
        val claimStatusUiState = claimStatusCardsUiState[page]
        ClaimStatusCard(
          uiState = claimStatusUiState,
          onClick = onClick,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      Spacer(Modifier.height(16.dp))

      HorizontalPagerIndicator(
        pagerState = pagerState,
        pageCount = claimStatusCardsUiState.size,
        activeColor = LocalContentColor.current,
        modifier = Modifier.padding(contentPadding).align(Alignment.CenterHorizontally),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimStatusCards() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ClaimStatusCards(
        onClick = {},
        contentPadding = PaddingValues(horizontal = 16.dp),
        claimStatusCardsUiState = List(3) {
          ClaimStatusCardUiState(
            id = "id#$it",
            pillTypes = listOf(ClaimPillType.Open, ClaimPillType.Closed.NotCompensated),
            claimProgressItemsUiState = listOf(
              ClaimProgressSegment(ClaimProgressSegment.SegmentText.Closed, ClaimProgressSegment.SegmentType.PAID),
            ),
            claimType = "Broken item",
            insuranceDisplayName = "Home Insurance Homeowner",
            submittedDate = Instant.parse("2024-05-01T00:00:00Z"),
          )
        }.toNonEmptyListOrNull()!!,
      )
    }
  }
}
