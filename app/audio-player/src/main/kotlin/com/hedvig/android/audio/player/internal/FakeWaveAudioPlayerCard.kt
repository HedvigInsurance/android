package com.hedvig.android.audio.player.internal

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import com.hedvig.android.audio.player.WaveInteraction
import com.hedvig.android.audio.player.state.AudioPlayerState
import com.hedvig.android.audio.player.state.AudioPlayerState.Ready.ReadyState
import com.hedvig.android.core.common.android.ProgressPercentage
import com.hedvig.android.core.designsystem.material3.squircle
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.designsystem.theme.onWarning
import com.hedvig.android.core.designsystem.theme.warning

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
    targetValue = cardColorForState(audioPlayerState),
    animationSpec = tween(500),
    label = "color",
  )
  val contentColor by animateColorAsState(
    targetValue = cardContentColorForState(audioPlayerState),
    animationSpec = tween(500),
    label = "contentColor",
  )
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = MaterialTheme.shapes.squircle,
    color = color,
    contentColor = contentColor,
  ) {
    FakeWaveAudioPlayerContent(
      audioPlayerState = audioPlayerState,
      startPlaying = startPlaying,
      pause = pause,
      retryLoadingAudio = retryLoadingAudio,
      waveInteraction = waveInteraction,
    )
  }
}

@Composable
private fun cardColorForState(audioPlayerState: AudioPlayerState): Color = when {
  audioPlayerState.isFailed -> androidx.compose.material.MaterialTheme.colors.warning
  else -> MaterialTheme.colorScheme.surface
}

@Composable
private fun cardContentColorForState(audioPlayerState: AudioPlayerState): Color = when {
  audioPlayerState.isFailed -> androidx.compose.material.MaterialTheme.colors.onWarning
  else -> androidx.compose.material3.MaterialTheme.colorScheme.onSurface
}

@HedvigPreview
@Composable
private fun PreviewFakeWaveAudioPlayerCard(
  @PreviewParameter(AudioPlayerStateProvider::class) audioPlayerState: AudioPlayerState,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
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
    Surface(color = MaterialTheme.colorScheme.background) {
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
