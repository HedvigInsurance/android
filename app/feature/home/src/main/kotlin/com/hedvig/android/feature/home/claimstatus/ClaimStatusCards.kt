package com.hedvig.android.feature.home.claimstatus

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.feature.home.claimdetail.ui.previewList
import com.hedvig.android.feature.home.claimstatus.claimprogress.ClaimProgressUiState
import com.hedvig.android.feature.home.claimstatus.data.ClaimStatusCardUiState
import com.hedvig.android.feature.home.claimstatus.data.PillUiState
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ClaimStatusCards(
  goToDetailScreen: ((claimId: String) -> Unit)?,
  onClaimCardShown: (String) -> Unit,
  claimStatusCardsUiState: NonEmptyList<ClaimStatusCardUiState>,
) {
  val pagerState = rememberPagerState()
  Column {
    HorizontalPager(
      pageCount = claimStatusCardsUiState.size,
      state = pagerState,
      contentPadding = PaddingValues(horizontal = 16.dp),
      beyondBoundsPageCount = 1,
      pageSpacing = 12.dp,
      key = { index -> claimStatusCardsUiState[index].id },
      modifier = Modifier.fillMaxWidth(),
    ) { page: Int ->
      val claimStatusUiState = claimStatusCardsUiState[page]
      ClaimStatusCard(
        uiState = claimStatusUiState,
        goToDetailScreen = goToDetailScreen,
        modifier = Modifier.fillMaxWidth(),
        onClaimCardShown = onClaimCardShown,
      )
    }
    if (claimStatusCardsUiState.size == 1) {
      Spacer(Modifier.height(14.dp))
    } else {
      Spacer(Modifier.height(16.dp))

      HorizontalPagerIndicator(
        pagerState = pagerState,
        pageCount = claimStatusCardsUiState.size,
        modifier = Modifier.align(Alignment.CenterHorizontally),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewClaimStatusCards() {
  HedvigTheme {
    Surface(color = MaterialTheme.colors.background) {
      val claimStatusCardsUiState = ClaimStatusCardUiState(
        id = UUID.randomUUID().toString(),
        pillsUiState = PillUiState.previewList(),
        title = "All-risk",
        subtitle = "Contents insurance",
        claimProgressItemsUiState = ClaimProgressUiState.previewList(),
      )
      ClaimStatusCards(
        goToDetailScreen = {},
        claimStatusCardsUiState = nonEmptyListOf(claimStatusCardsUiState),
        onClaimCardShown = {},
      )
    }
  }
}
