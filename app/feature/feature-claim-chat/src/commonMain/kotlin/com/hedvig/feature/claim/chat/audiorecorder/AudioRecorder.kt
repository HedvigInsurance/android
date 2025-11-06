package com.hedvig.feature.claim.chat.audiorecorder

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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hedvig.feature.claim.chat.assistantmessage.ThreeDotLoadingIndicator
import com.hedvig.feature.claim.chat.audiorecorder.AudioRecorderUiState.AudioRecording.Playback
import com.hedvig.feature.claim.chat.audiorecorder.AudioRecorderUiState.AudioRecording.PrerecordedWithAudioContent
import com.hedvig.feature.claim.chat.audiorecorder.AudioRecorderUiState.AudioRecording.Recording
import com.hedvig.feature.claim.chat.audiorecorder.audioplayer.PlayableAudioSource
import com.hedvig.feature.claim.chat.audiorecorder.audioplayer.rememberAudioPlayer
import kotlin.time.Instant

@Composable
internal fun AudioRecorder(
  uiState: AudioRecorderUiState,
  startRecording: () -> Unit,
  stopRecording: () -> Unit,
  submitAudioFile: () -> Unit,
  submitAudioUrl: (AudioUrl) -> Unit,
  redo: () -> Unit,
  modifier: Modifier = Modifier,
) {
  when (uiState) {
    is Playback -> Playback(
      uiState = uiState,
      submit = {
        val filePath = uiState.filePath
        submitAudioFile()
      },
      redo = redo,
      modifier = modifier,
    )

    is PrerecordedWithAudioContent -> PrerecordedPlayback(
      uiState = uiState,
      submitAudioUrl = {
        submitAudioUrl(uiState.audioContent.audioUrl)
      },
      redo = redo,
      modifier = modifier,
    )

    else -> {
      val isRecording = uiState is Recording
      val isRecordingTransition = updateTransition(isRecording)
      if (isRecording) {
        // TODO ScreenOnFlag()
      }

      val startRecordingText = "Start recording"
      val stopRecordingText = "Stop recording"
      val audioRecordingText = "Recording..."
      val recordingState = "Recording now"

      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
      ) {
        Box(
          contentAlignment = Alignment.Center,
        ) {
          if (uiState is Recording && uiState.amplitudes.isNotEmpty()) {
            RecordingAmplitudeIndicator(amplitude = uiState.amplitudes.last())
          }
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .shadow(2.dp, CircleShape)
              .size(72.dp)
              .background(Color.White, CircleShape)
              .semantics {
                contentDescription = audioRecordingText
                stateDescription = if (isRecording) recordingState else ""
              }
              .clickable(
                onClickLabel = when (isRecording) {
                  true -> stopRecordingText
                  // todo: this one is not working somehow,
                  // so added onClickLabel inside val recordingState
                  false -> startRecordingText
                },
                role = Role.Button,
              ) {
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
              if (isRecording) Color.Black else Color.Red
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
          if (uiState is Recording) {
            value = uiState.startedAt
          }
        }
        // TODO val twoDigitsFormat = remember { DecimalFormat("00") }

        isRecordingTransition.AnimatedContent(
          transitionSpec = {
            val animationSpec = tween<IntOffset>(450)
            val animationSpecFade = tween<Float>(250)
            val animationSpecFloat = tween<Float>(450)
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
            // TODO val diff = clock.now() - (startedRecordingAt ?: clock.now())
            // TODO val label = "${twoDigitsFormat.format(diff.inWholeMinutes)}:${twoDigitsFormat.format(diff.inWholeSeconds % 60)}"
            // TODO val durationDescription = stringResource(R.string.TALKBACK_RECORDING_DURATION, label)
            Text(
              text = "",
              style = MaterialTheme.typography.bodySmall,
              textAlign = TextAlign.Center,
              modifier = Modifier
                .padding(bottom = 16.dp),
            )
          } else {
            Text(
              text = startRecordingText,
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
private fun Playback(uiState: Playback, submit: () -> Unit, redo: () -> Unit, modifier: Modifier = Modifier) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
  ) {
    if (!uiState.isPrepared) {
      ThreeDotLoadingIndicator()
    } else {
      val audioPlayer = rememberAudioPlayer(PlayableAudioSource.LocalFilePath(uiState.filePath))
      HedvigAudioPlayer(audioPlayer = audioPlayer)
    }

    Button(
      onClick = submit,
      // TODO isLoading = uiState.isLoading,
      enabled = uiState.canSubmit,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp),
    ) {
      Text("Save and continue")
    }

    TextButton(
      onClick = redo,
      enabled = uiState.canSubmit,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp),
    ) {
      Text("Record again")
    }
  }
}

@Composable
fun PrerecordedPlayback(
  uiState: PrerecordedWithAudioContent,
  redo: () -> Unit,
  submitAudioUrl: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
  ) {

    AnimatedVisibility(
      visible = true,
      enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically, clip = false),
      exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically, clip = false),
    ) {
      Column {
        Spacer(Modifier.height(16.dp))
        Button(
          onClick = submitAudioUrl,
          enabled = uiState.canSubmit,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text("Continue")
        }
      }
    }

    AnimatedVisibility(
      visible = true,
      enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically, clip = false),
      exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically, clip = false),
    ) {
      Column {
        Spacer(Modifier.height(8.dp))
        TextButton(
          onClick = redo,
          enabled = uiState.canSubmit,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text("Record again")
        }
      }
    }
  }
}
