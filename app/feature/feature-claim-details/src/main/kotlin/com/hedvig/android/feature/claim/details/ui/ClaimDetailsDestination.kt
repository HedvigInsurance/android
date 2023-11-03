package com.hedvig.android.feature.claim.details.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.SignedAudioUrl
import com.hedvig.android.audio.player.state.AudioPlayerState
import com.hedvig.android.audio.player.state.PlayableAudioSource
import com.hedvig.android.audio.player.state.rememberAudioPlayer
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.preview.HedvigMultiScreenPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.colored.hedvig.Chat
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.ui.claimstatus.ClaimStatusCard
import com.hedvig.android.ui.claimstatus.model.ClaimPillType
import com.hedvig.android.ui.claimstatus.model.ClaimProgressSegment
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import hedvig.resources.R
import octopus.type.CurrencyCode

@Composable
internal fun ClaimDetailsDestination(
  viewModel: ClaimDetailsViewModel,
  navigateUp: () -> Unit,
  onChatClick: () -> Unit,
) {
  val viewState by viewModel.uiState.collectAsStateWithLifecycle()
  ClaimDetailScreen(
    uiState = viewState,
    retry = { viewModel.emit(ClaimDetailsEvent.Retry) },
    navigateUp = navigateUp,
    onChatClick = onChatClick,
  )
}

@Composable
private fun ClaimDetailScreen(
  uiState: ClaimDetailUiState,
  retry: () -> Unit,
  navigateUp: () -> Unit,
  onChatClick: () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.background,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column(Modifier.fillMaxSize()) {
      TopAppBarWithBack(
        onClick = navigateUp,
        title = stringResource(hedvig.resources.R.string.CLAIMS_YOUR_CLAIM),
        contentPadding = WindowInsets.safeDrawing
          .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
          .asPaddingValues(),
      )
      when (uiState) {
        is ClaimDetailUiState.Content -> ClaimDetailScreen(
          uiState = uiState,
          onChatClick = onChatClick,
        )
        ClaimDetailUiState.Error -> HedvigErrorSection(retry = retry)
        ClaimDetailUiState.Loading -> HedvigFullScreenCenterAlignedProgressDebounced()
      }
    }
  }
}

@Composable
private fun ClaimDetailScreen(
  uiState: ClaimDetailUiState.Content,
  onChatClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .verticalScroll(rememberScrollState())
      .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
      .padding(horizontal = 16.dp),
  ) {
    Spacer(Modifier.height(8.dp))
    ClaimStatusCard(
      uiState = uiState.claimStatusCardUiState,
      onClick = null,
    )
    Spacer(Modifier.height(8.dp))
    ClaimInfoCard(uiState.claimStatus, uiState.claimOutcome, onChatClick)
    when (uiState.submittedContent) {
      is ClaimDetailUiState.Content.SubmittedContent.Audio -> {
        Spacer(Modifier.height(40.dp))
        ClaimDetailHedvigAudioPlayerItem(uiState.submittedContent.signedAudioURL)
      }
      is ClaimDetailUiState.Content.SubmittedContent.FreeText -> {
        Spacer(Modifier.height(40.dp))
        Text(
          stringResource(R.string.claim_status_detail_submitted_message),
          Modifier.padding(horizontal = 2.dp),
        )
        Spacer(Modifier.height(2.dp))
        HedvigCard {
          Text(
            uiState.submittedContent.text,
            Modifier.padding(16.dp),
          )
        }
      }
      else -> {}
    }
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
internal fun ClaimInfoCard(
  claimStatus: ClaimDetailUiState.Content.ClaimStatus,
  claimOutcome: ClaimDetailUiState.Content.ClaimOutcome,
  onChatClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(modifier = modifier) {
    Column {
      val claimIsInUndeterminedState = claimStatus == ClaimDetailUiState.Content.ClaimStatus.CLOSED &&
        claimOutcome == ClaimDetailUiState.Content.ClaimOutcome.UNKNOWN
      if (!claimIsInUndeterminedState) {
        Text(
          text = statusParagraphText(claimStatus, claimOutcome),
          style = MaterialTheme.typography.bodyLarge,
          modifier = Modifier.padding(16.dp),
        )
        Spacer(Modifier.height(4.dp))
        Divider()
      }
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .padding(16.dp)
          .fillMaxWidth(),
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = stringResource(R.string.claim_status_contact_generic_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = stringResource(R.string.claim_status_contact_generic_title),
            style = MaterialTheme.typography.bodyLarge,
          )
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = onChatClick) {
          Icon(Icons.Hedvig.Chat, stringResource(R.string.claim_status_detail_chat_button_description))
        }
      }
    }
  }
}

@Composable
private fun statusParagraphText(
  claimStatus: ClaimDetailUiState.Content.ClaimStatus,
  claimOutcome: ClaimDetailUiState.Content.ClaimOutcome,
): String = when (claimStatus) {
  ClaimDetailUiState.Content.ClaimStatus.CREATED -> stringResource(R.string.claim_status_submitted_support_text)
  ClaimDetailUiState.Content.ClaimStatus.IN_PROGRESS -> stringResource(R.string.claim_status_being_handled_support_text)
  ClaimDetailUiState.Content.ClaimStatus.CLOSED -> when (claimOutcome) {
    ClaimDetailUiState.Content.ClaimOutcome.PAID -> stringResource(R.string.claim_status_paid_support_text)
    ClaimDetailUiState.Content.ClaimOutcome.NOT_COMPENSATED -> {
      stringResource(R.string.claim_status_not_compensated_support_text)
    }
    ClaimDetailUiState.Content.ClaimOutcome.NOT_COVERED -> {
      stringResource(R.string.claim_status_not_covered_support_text)
    }
    ClaimDetailUiState.Content.ClaimOutcome.UNKNOWN -> ""
  }
  ClaimDetailUiState.Content.ClaimStatus.REOPENED -> {
    stringResource(R.string.claim_status_being_handled_reopened_support_text)
  }
  ClaimDetailUiState.Content.ClaimStatus.UNKNOWN -> stringResource(R.string.claim_status_being_handled_support_text)
}

@Composable
private fun ClaimDetailHedvigAudioPlayerItem(
  signedAudioUrl: SignedAudioUrl,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    val audioPlayer = rememberAudioPlayer(playableAudioSource = PlayableAudioSource.RemoteUrl(signedAudioUrl))
    HedvigAudioPlayer(audioPlayer = audioPlayer)
    Spacer(Modifier.height(8.dp))
    val audioPlayerState by audioPlayer.audioPlayerState.collectAsStateWithLifecycle()
    AnimatedVisibility(visible = audioPlayerState !is AudioPlayerState.Failed) {
      Text(
        text = stringResource(R.string.claim_status_files_claim_audio_footer),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@HedvigMultiScreenPreview
@Composable
private fun PreviewClaimDetailScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      ClaimDetailScreen(
        uiState = ClaimDetailUiState.Content(
          claimId = "id",
          submittedContent = ClaimDetailUiState.Content.SubmittedContent.FreeText("Some free input text"),
          claimStatusCardUiState = ClaimStatusCardUiState(
            id = "id",
            pillTypes = listOf(ClaimPillType.Closed.Paid, ClaimPillType.PaymentAmount(UiMoney(399.0, CurrencyCode.SEK))),
            claimProgressItemsUiState = listOf(
              ClaimProgressSegment(ClaimProgressSegment.SegmentText.Closed, ClaimProgressSegment.SegmentType.PAID),
            ),
          ),
          claimStatus = ClaimDetailUiState.Content.ClaimStatus.CLOSED,
          claimOutcome = ClaimDetailUiState.Content.ClaimOutcome.PAID,
        ),
        onChatClick = {},
      )
    }
  }
}
