package com.hedvig.android.feature.odyssey.step.audiorecording.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audio_player_data.AudioPlayer
import com.example.audio_player_data.AudioPlayerState
import com.example.audio_player_data.PlayableAudioSource
import com.example.audio_player_data.SignedAudioUrl
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.audioplayer.rememberAudioPlayer
import com.hedvig.android.core.common.android.ProgressPercentage
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.material3.motion.MotionTokens
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.ScreenOnFlag
import com.hedvig.android.core.ui.audiorecording.RecordingAmplitudeIndicator
import com.hedvig.android.data.claimflow.AudioContent
import com.hedvig.android.data.claimflow.model.AudioUrl
import com.hedvig.android.feature.odyssey.step.audiorecording.AudioRecordingUiState
import hedvig.resources.R
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Composable
internal fun AudioRecorder(
  uiState: AudioRecordingUiState,
  startRecording: () -> Unit,
  clock: Clock,
  stopRecording: () -> Unit,
  submitAudioFile: (File) -> Unit,
  submitAudioUrl: (AudioUrl) -> Unit,
  redo: () -> Unit,
  modifier: Modifier = Modifier,
) {
  when (uiState) {
    is AudioRecordingUiState.Playback -> Playback(
      uiState = uiState,
      submit = {
        val filePath = uiState.filePath
        val audioFile = File(filePath)
        submitAudioFile(audioFile)
      },
      redo = redo,
      modifier = modifier,
    )
    is AudioRecordingUiState.PrerecordedWithAudioContent -> PrerecordedPlayback(
      uiState = uiState,
      submitAudioUrl = {
        submitAudioUrl(uiState.audioContent.audioUrl)
      },
      redo = redo,
      modifier = modifier,
    )
    else -> {
      val isRecording = uiState is AudioRecordingUiState.Recording
      val isRecordingTransition = updateTransition(isRecording)
      if (isRecording) {
        ScreenOnFlag()
      }

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
      ) {
        Box(
          contentAlignment = Alignment.Center,
        ) {
          if (uiState is AudioRecordingUiState.Recording && uiState.amplitudes.isNotEmpty()) {
            RecordingAmplitudeIndicator(amplitude = uiState.amplitudes.last())
          }
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .shadow(10.dp, CircleShape)
              .size(72.dp)
              .background(Color.White, CircleShape)
              .clickable {
                if (isRecording) {
                  stopRecording()
                } else {
                  startRecording()
                }
              },
          ) {
            val size by isRecordingTransition.animateDp(label = "sizeAnimation") { isRecording ->
              if (isRecording) 18.dp else 32.dp
            }
            val color by isRecordingTransition.animateColor(label = "colorAnimation") { isRecording ->
              if (isRecording) Color.Black else MaterialTheme.colorScheme.error
            }
            val cornerRadius by isRecordingTransition.animateDp(label = "cornerRadiusAnimation") { isRecording ->
              if (isRecording) 2.dp else 16.dp
            }
            Box(
              Modifier
                .size(size)
                .background(color, RoundedCornerShape(cornerRadius)),
            )
          }
        }
        Spacer(Modifier.height(24.dp))
        val startedRecordingAt by remember {
          mutableStateOf<Instant?>(null)
        }.apply {
          if (uiState is AudioRecordingUiState.Recording) {
            value = uiState.startedAt
          }
        }
        isRecordingTransition.AnimatedContent(
          transitionSpec = {
            val animationSpec = tween<IntOffset>(MotionTokens.DurationLong1.toInt())
            val animationSpecFade = tween<Float>(MotionTokens.DurationMedium1.toInt())
            val animationSpecFloat = tween<Float>(MotionTokens.DurationLong1.toInt())
            val scale = 0.6f
            val enterTransition =
              slideInVertically(animationSpec) +
                fadeIn(animationSpecFade) +
                scaleIn(animationSpecFloat, initialScale = scale)
            val exitTransition =
              slideOutVertically(animationSpec) +
                fadeOut(animationSpecFade) +
                scaleOut(animationSpecFloat, targetScale = scale)
            enterTransition togetherWith exitTransition
          },
          contentAlignment = Alignment.Center,
          modifier = Modifier.fillMaxWidth(),
        ) { isRecording ->
          if (isRecording) {
            val diff = clock.now() - (startedRecordingAt ?: clock.now())
            val label = String.format("%02d:%02d", diff.inWholeMinutes, diff.inWholeSeconds % 60)
            Text(
              text = label,
              style = MaterialTheme.typography.bodySmall,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(bottom = 16.dp),
            )
          } else {
            Text(
              text = stringResource(R.string.EMBARK_START_RECORDING),
              style = MaterialTheme.typography.bodySmall,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(bottom = 16.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun Playback(
  uiState: AudioRecordingUiState.Playback,
  submit: () -> Unit,
  redo: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
  ) {
    if (!uiState.isPrepared) {
      CircularProgressIndicator()
    } else {
      val audioPlayer = rememberAudioPlayer(PlayableAudioSource.LocalFilePath(uiState.filePath))
      HedvigAudioPlayer(audioPlayer = audioPlayer)
    }

    HedvigContainedButton(
      onClick = submit,
      text = stringResource(R.string.SAVE_AND_CONTINUE_BUTTON_LABEL),
      isLoading = uiState.isLoading,
      enabled = uiState.canSubmit,
      modifier = Modifier.padding(top = 16.dp),
    )

    HedvigTextButton(
      text = stringResource(R.string.EMBARK_RECORD_AGAIN),
      onClick = redo,
      enabled = uiState.canSubmit,
      modifier = Modifier.padding(top = 8.dp),
    )
  }
}

@Composable
private fun PrerecordedPlayback(
  uiState: AudioRecordingUiState.PrerecordedWithAudioContent,
  redo: () -> Unit,
  submitAudioUrl: () -> Unit,
  modifier: Modifier = Modifier,
  audioPlayer: AudioPlayer = rememberAudioPlayer(
    PlayableAudioSource.RemoteUrl(SignedAudioUrl.fromSignedAudioUrlString(uiState.audioContent.signedUrl.value)),
  ),
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
  ) {
    val audioPlayerState = audioPlayer.audioPlayerState.collectAsStateWithLifecycle().value
    HedvigAudioPlayer(audioPlayer = audioPlayer)

    AnimatedVisibility(
      visible = audioPlayerState is AudioPlayerState.Ready,
      enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically, clip = false),
      exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically, clip = false),
    ) {
      Column {
        Spacer(Modifier.height(16.dp))
        HedvigContainedButton(
          onClick = submitAudioUrl,
          text = stringResource(R.string.general_continue_button),
          isLoading = uiState.isLoading,
          enabled = uiState.canSubmit,
        )
      }
    }

    AnimatedVisibility(
      visible = audioPlayerState is AudioPlayerState.Failed || audioPlayerState is AudioPlayerState.Ready,
      enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically, clip = false),
      exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically, clip = false),
    ) {
      Column {
        Spacer(Modifier.height(8.dp))
        HedvigTextButton(
          text = stringResource(R.string.EMBARK_RECORD_AGAIN),
          onClick = redo,
          enabled = uiState.canSubmit,
        )
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewNotRecording() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AudioRecorder(
        uiState = AudioRecordingUiState.NotRecording,
        startRecording = { },
        clock = Clock.System,
        stopRecording = { },
        submitAudioFile = {},
        submitAudioUrl = {},
        redo = { },
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewRecording() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AudioRecorder(
        uiState = AudioRecordingUiState.Recording(listOf(70), Clock.System.now(), ""),
        startRecording = { },
        clock = Clock.System,
        stopRecording = { },
        submitAudioFile = {},
        submitAudioUrl = {},
        redo = { },
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewPrerecordedPlayback() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      PrerecordedPlayback(
        uiState = AudioRecordingUiState.PrerecordedWithAudioContent(AudioContent(AudioUrl(""), AudioUrl(""))),
        redo = {},
        submitAudioUrl = {},
        modifier = Modifier,
        audioPlayer = object : AudioPlayer {
          override val audioPlayerState: StateFlow<AudioPlayerState> = MutableStateFlow(
            AudioPlayerState.Ready.done(),
          )

          override fun initialize() = error("Not implemented")

          override fun startPlayer() = error("Not implemented")

          override fun pausePlayer() = error("Not implemented")

          override fun retryLoadingAudio() = error("Not implemented")

          override fun seekTo(progressPercentage: ProgressPercentage) = error("Not implemented")

          override fun close() = error("Not implemented")
        },
      )
    }
  }
}
