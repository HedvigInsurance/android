package com.hedvig.feature.claim.chat.audiorecorder.audioplayer.audiowaves

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import com.hedvig.feature.claim.chat.assistantmessage.ThreeDotLoadingIndicator
import com.hedvig.feature.claim.chat.audiorecorder.audioplayer.AudioPlayerState

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
      audioPlayerState.isFailed -> Color.Red
      else -> Color.Transparent
    },
    animationSpec = tween(500),
    label = "color",
  )
  val contentColor by animateColorAsState(
    targetValue = when {
      audioPlayerState.isFailed -> Color.Red
      else -> Color.Transparent
    },
    animationSpec = tween(500),
    label = "contentColor",
  )
  Surface(
    modifier = modifier
      .widthIn(max = 500.dp)
      .fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
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
        // TODO
      }

      AudioPlayerState.Preparing,
      is AudioPlayerState.Ready,
        -> {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .height(64.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp),
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
                .compositeOver(Color.Transparent),
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
        ThreeDotLoadingIndicator()
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
              AudioPlayerState.Ready.ReadyState.Playing -> Pause
              AudioPlayerState.Ready.ReadyState.Seeking -> Pause
              else -> Play
            },
            contentDescription = "Pause",
            modifier = Modifier.size(24.dp),
          )
        }
      }

      is AudioPlayerState.Failed -> error("Impossible")
    }
  }
}

