package com.hedvig.android.audio.player.internal

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.NotificationDefaults.InfoCardStyle.Button
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Pause
import com.hedvig.android.design.system.hedvig.icon.Play
import com.hedvig.audio.player.data.AudioPlayerState
import com.hedvig.audio.player.data.AudioPlayerState.Ready.ReadyState
import com.hedvig.audio.player.data.ProgressPercentage
import hedvig.resources.R

@Composable
internal fun FakeWaveAudioPlayerCard(
  audioPlayerState: AudioPlayerState,
  startPlaying: () -> Unit,
  pause: () -> Unit,
  retryLoadingAudio: () -> Unit,
  waveInteraction: WaveInteraction,
  modifier: Modifier = Modifier,
) {
  val color by animateColorAsState(
    targetValue = when {
      audioPlayerState.isFailed -> HedvigTheme.colorScheme.signalAmberFill
      else -> HedvigTheme.colorScheme.surfacePrimary
    },
    animationSpec = tween(500),
    label = "color",
  )
  val contentColor by animateColorAsState(
    targetValue = when {
      audioPlayerState.isFailed -> HedvigTheme.colorScheme.signalAmberText
      else -> HedvigTheme.colorScheme.fillPrimary
    },
    animationSpec = tween(500),
    label = "contentColor",
  )
  Surface(
    modifier = modifier
      .widthIn(max = 500.dp)
      .fillMaxWidth(),
    shape = HedvigTheme.shapes.cornerLarge,
    color = color,
    contentColor = contentColor,
  ) {
    FakeWaveAudioPlayerContent(
      audioPlayerState = audioPlayerState,
      startPlaying = startPlaying,
      pause = pause,
      retryLoadingAudio = retryLoadingAudio,
      waveInteraction = waveInteraction,
      modifier = Modifier.animateContentSize(animationSpec = spring(stiffness = 400f)),
    )
  }
}

@Composable
private fun FakeWaveAudioPlayerContent(
  audioPlayerState: AudioPlayerState,
  startPlaying: () -> Unit,
  pause: () -> Unit,
  retryLoadingAudio: () -> Unit,
  waveInteraction: WaveInteraction,
  modifier: Modifier = Modifier,
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier,
  ) {
    when (audioPlayerState) {
      AudioPlayerState.Failed -> {
        HedvigNotificationCard(
          content = {
            Column {
              HedvigText(text = stringResource(R.string.claim_status_detail_info_error_title))
              HedvigText(
                text = stringResource(R.string.claim_status_detail_info_error_body),
                style = HedvigTheme.typography.finePrint,
              )
            }
          },
          priority = NotificationPriority.Attention,
          style = Button(
            buttonText = stringResource(R.string.claim_status_detail_info_error_button),
            onButtonClick = retryLoadingAudio,
          ),
        )
      }

      AudioPlayerState.Preparing,
      is AudioPlayerState.Ready,
      -> {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .height(64.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
          ActionOrLoadingIcon(
            audioPlayerState = audioPlayerState,
            startPlaying = startPlaying,
            pause = pause,
          )
          if (audioPlayerState is AudioPlayerState.Ready) {
            FakeAudioWaves(
              progressPercentage = audioPlayerState.progressPercentage,
              playedColor = LocalContentColor.current,
              notPlayedColor = LocalContentColor.current.copy(0.38f)
                .compositeOver(HedvigTheme.colorScheme.surfacePrimary),
              waveInteraction = waveInteraction,
              modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(20.dp))
          }
        }
      }
    }
  }
}

@Composable
private fun ActionOrLoadingIcon(
  // AudioPlayerState.Preparing | AudioPlayerState.Ready
  audioPlayerState: AudioPlayerState,
  startPlaying: () -> Unit,
  pause: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.size(48.dp),
    contentAlignment = Alignment.Center,
    propagateMinConstraints = true,
  ) {
    when (audioPlayerState) {
      is AudioPlayerState.Preparing -> {
        HedvigCircularProgressIndicator(
          modifier = Modifier
            .wrapContentSize()
            .size(24.dp),
        )
      }

      is AudioPlayerState.Ready -> {
        IconButton(
          onClick = when (audioPlayerState.readyState) {
            is AudioPlayerState.Ready.ReadyState.Playing -> pause
            else -> startPlaying
          },
        ) {
          Icon(
            imageVector = when (audioPlayerState.readyState) {
              AudioPlayerState.Ready.ReadyState.Playing -> HedvigIcons.Pause
              AudioPlayerState.Ready.ReadyState.Seeking -> HedvigIcons.Pause
              else -> HedvigIcons.Play
            },
            contentDescription = stringResource(
              when (audioPlayerState.readyState) {
                AudioPlayerState.Ready.ReadyState.Playing -> R.string.A11Y_PAUSE
                AudioPlayerState.Ready.ReadyState.Seeking -> R.string.A11Y_PAUSE
                else -> R.string.A11Y_PLAY
              },
            ),
            modifier = Modifier.size(24.dp),
          )
        }
      }

      is AudioPlayerState.Failed -> error("Impossible")
    }
  }
}

@HedvigPreview
@PreviewScreenSizes
@Composable
private fun PreviewFakeWaveAudioPlayerCard(
  @PreviewParameter(AudioPlayerStateProvider::class) audioPlayerState: AudioPlayerState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      FakeWaveAudioPlayerCard(audioPlayerState, {}, {}, {}, {})
    }
  }
}

private class AudioPlayerStateProvider : CollectionPreviewParameterProvider<AudioPlayerState>(
  listOf(
    AudioPlayerState.Failed,
    AudioPlayerState.Ready(ReadyState.Paused, ProgressPercentage(0.4f)),
    AudioPlayerState.Ready(ReadyState.Playing, ProgressPercentage(0.6f)),
  ),
)

@HedvigPreview
@Composable
private fun PreviewFakeWaveAudioPlayerCardAnimation() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      var audioPlayerState: AudioPlayerState by remember {
        mutableStateOf(AudioPlayerState.Preparing)
      }
      FakeWaveAudioPlayerCard(
        audioPlayerState = audioPlayerState,
        {},
        {},
        {},
        {},
        modifier = Modifier.clickable {
          audioPlayerState = when (audioPlayerState) {
            AudioPlayerState.Failed -> AudioPlayerState.Preparing
            AudioPlayerState.Preparing -> AudioPlayerState.Ready.done()
            is AudioPlayerState.Ready -> AudioPlayerState.Failed
          }
        },
      )
    }
  }
}
