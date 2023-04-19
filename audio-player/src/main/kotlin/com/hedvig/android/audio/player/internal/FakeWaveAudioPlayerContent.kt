package com.hedvig.android.audio.player.internal

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.audio.player.WaveInteraction
import com.hedvig.android.audio.player.state.AudioPlayerState
import com.hedvig.android.core.common.android.ProgressPercentage
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.designsystem.theme.hedvig_off_white
import com.hedvig.android.core.designsystem.theme.lavender_600

@Composable
internal fun FakeWaveAudioPlayerContent(
  audioPlayerState: AudioPlayerState,
  startPlaying: () -> Unit,
  pause: () -> Unit,
  retryLoadingAudio: () -> Unit,
  waveInteraction: WaveInteraction,
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier.animateContentSize(animationSpec = spring(stiffness = 400f)),
  ) {
    when (audioPlayerState) {
      AudioPlayerState.Failed -> {
        FailedAudioPlayerCard(tryAgain = retryLoadingAudio)
      }
      AudioPlayerState.Preparing,
      is AudioPlayerState.Ready,
      -> {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .height(64.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessLow)),
        ) {
          AudioPlayerActionOrLoadingIcon(
            audioPlayerState = audioPlayerState,
            startPlaying = startPlaying,
            pause = pause,
            modifier = Modifier.size(48.dp),
          )
          if (audioPlayerState is AudioPlayerState.Ready) {
            FakeAudioWaves(
              progressPercentage = audioPlayerState.progressPercentage,
              playedColor = if (isSystemInDarkTheme()) {
                hedvig_off_white
              } else {
                lavender_600
              },
              notPlayedColor = LocalContentColor.current,
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
private fun AudioPlayerActionOrLoadingIcon(
  audioPlayerState: AudioPlayerState, // AudioPlayerState.Preparing | AudioPlayerState.Ready
  startPlaying: () -> Unit,
  pause: () -> Unit,
  modifier: Modifier = Modifier,
) {
  require(audioPlayerState !is AudioPlayerState.Failed)
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    when (audioPlayerState) {
      is AudioPlayerState.Preparing -> {
        CircularProgressIndicator(
          color = LocalContentColor.current,
          modifier = Modifier.size(24.dp),
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
              AudioPlayerState.Ready.ReadyState.Playing -> Icons.Default.Pause
              AudioPlayerState.Ready.ReadyState.Seeking -> Icons.Default.Pause
              else -> Icons.Default.PlayArrow
            },
            contentDescription = null,
          )
        }
      }
      is AudioPlayerState.Failed -> error("Impossible")
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewFakeWaveAudioPlayerContent() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      FakeWaveAudioPlayerContent(
        AudioPlayerState.Ready(AudioPlayerState.Ready.ReadyState.Playing, ProgressPercentage(0.4f)), {}, {}, {}, {},
      )
    }
  }
}
