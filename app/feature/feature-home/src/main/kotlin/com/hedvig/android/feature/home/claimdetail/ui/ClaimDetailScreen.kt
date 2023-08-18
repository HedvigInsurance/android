package com.hedvig.android.feature.home.claimdetail.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.preview.HedvigMultiScreenPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.feature.home.claimdetail.model.ClaimDetailUiState
import com.hedvig.android.feature.home.claimstatus.TopInfo
import com.hedvig.android.feature.home.claimstatus.claimprogress.ClaimProgressRow

@Composable
internal fun ClaimDetailScreen(
  viewState: ClaimDetailViewState,
  retry: () -> Unit,
  onUpClick: () -> Unit,
  onChatClick: () -> Unit,
  onPlayClick: () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.background,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column(Modifier.fillMaxSize()) {
      TopAppBarWithBack(
        onClick = onUpClick,
        title = stringResource(hedvig.resources.R.string.CLAIMS_YOUR_CLAIM),
        contentPadding = WindowInsets.safeDrawing
          .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
          .asPaddingValues(),
      )
      when (viewState) {
        is ClaimDetailViewState.Content -> ClaimDetailScreen(
          uiState = viewState.uiState,
          onChatClick = onChatClick,
          onPlayClick = onPlayClick,
        )

        ClaimDetailViewState.Error -> HedvigErrorSection(retry = retry)

        ClaimDetailViewState.Loading -> {
          HedvigFullScreenCenterAlignedProgress()
        }
      }
    }
  }
}

@Composable
private fun ClaimDetailScreen(
  uiState: ClaimDetailUiState,
  onChatClick: () -> Unit,
  onPlayClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .verticalScroll(rememberScrollState())
      .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
      .padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.height(8.dp))
    HedvigCard(
      colors = CardDefaults.elevatedCardColors(),
      modifier = modifier,
    ) {
      Column {
        TopInfo(
          pillsUiState = uiState.pillsUiState,
          title = uiState.claimType,
          subtitle = uiState.insuranceType,
          isClickable = false,
          modifier = Modifier.padding(16.dp),
        )
        Divider()
        ClaimProgressRow(
          claimProgressItemsUiState = uiState.claimDetailCard.claimProgressItemsUiState,
          modifier = Modifier.padding(16.dp),
        )

      }
    }
    Spacer(Modifier.height(8.dp))
    ClaimInfoCard(uiState.claimDetailCard, onChatClick)
    if (uiState.signedAudioURL != null) {
      Spacer(Modifier.height(40.dp))
      ClaimDetailHedvigAudioPlayerItem(
        onPlayClick = onPlayClick,
        uiState.signedAudioURL,
      )
    }
    Spacer(Modifier.height(16.dp))
  }
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewClaimDetailScreen() {
  HedvigTheme {
    Surface {
      ClaimDetailScreen(
        uiState = ClaimDetailUiState.previewData(),
        onPlayClick = {},
        onChatClick = {},
      )
    }
  }
}
