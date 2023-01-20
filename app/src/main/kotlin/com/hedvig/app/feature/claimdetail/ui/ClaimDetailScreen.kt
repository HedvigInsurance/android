package com.hedvig.app.feature.claimdetail.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.core.ui.genericinfo.GenericErrorScreen
import com.hedvig.app.feature.claimdetail.model.ClaimDetailResult
import com.hedvig.app.feature.claimdetail.model.ClaimDetailUiState
import com.hedvig.app.ui.compose.composables.CenteredProgressIndicator
import com.hedvig.app.util.compose.preview.previewData
import java.util.Locale

@Composable
fun ClaimDetailScreen(
  viewState: ClaimDetailViewState,
  retry: () -> Unit,
  onUpClick: () -> Unit,
  onChatClick: () -> Unit,
  onPlayClick: () -> Unit,
  locale: Locale,
) {
  Column {
    TopAppBarWithBack(
      onClick = onUpClick,
      title = stringResource(hedvig.resources.R.string.claim_status_title),
      contentPadding = WindowInsets.safeDrawing
        .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
        .asPaddingValues(),
    )
    when (viewState) {
      is ClaimDetailViewState.Content -> ClaimDetailScreen(
        uiState = viewState.uiState,
        locale = locale,
        onChatClick = onChatClick,
        onPlayClick = onPlayClick,
      )
      ClaimDetailViewState.Error -> GenericErrorScreen(
        onRetryButtonClick = retry,
        modifier = Modifier
          .padding(16.dp)
          .padding(top = 40.dp)
          .padding(
            WindowInsets.safeDrawing
              .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
              .asPaddingValues(),
          ),
      )
      ClaimDetailViewState.Loading -> CenteredProgressIndicator()
    }
  }
}

@Composable
private fun ClaimDetailScreen(
  uiState: ClaimDetailUiState,
  onChatClick: () -> Unit,
  onPlayClick: () -> Unit,
  locale: Locale,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .verticalScroll(rememberScrollState())
      .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
      .padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.height(24.dp))
    ClaimType(
      title = uiState.claimType,
      subtitle = uiState.insuranceType,
    )
    when (uiState.claimDetailResult) {
      ClaimDetailResult.Open -> {
        Spacer(Modifier.height(16.dp))
      }
      is ClaimDetailResult.Closed -> {
        Spacer(Modifier.height(20.dp))
        ClaimResultSection(uiState.claimDetailResult, locale)
        Spacer(Modifier.height(20.dp))
      }
    }
    SubmittedAndClosedColumns(uiState.submittedAt, uiState.closedAt, locale)
    Spacer(Modifier.height(24.dp))
    ClaimDetailCard(uiState.claimDetailCard, onChatClick)
    if (uiState.signedAudioURL != null) {
      Spacer(Modifier.height(40.dp))
      AudioPlayBackItem(
        onPlayClick = onPlayClick,
        uiState.signedAudioURL,
      )
    }
    Spacer(Modifier.height(16.dp))
  }
}

@Preview(showBackground = true)
@Composable
fun ClaimDetailScreenPreview() {
  HedvigTheme {
    ClaimDetailScreen(
      uiState = ClaimDetailUiState.previewData(),
      onPlayClick = {},
      locale = Locale.getDefault(),
      onChatClick = {},
    )
  }
}
