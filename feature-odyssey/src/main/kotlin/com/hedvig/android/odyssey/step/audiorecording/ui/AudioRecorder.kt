package com.hedvig.android.odyssey.step.audiorecording.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.SignedAudioUrl
import com.hedvig.android.audio.player.state.AudioPlayer
import com.hedvig.android.audio.player.state.AudioPlayerState
import com.hedvig.android.audio.player.state.PlayableAudioSource
import com.hedvig.android.audio.player.state.rememberAudioPlayer
import com.hedvig.android.core.common.android.ProgressPercentage
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.component.button.LargeTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.ScreenOnFlag
import com.hedvig.android.core.ui.audiorecording.RecordingAmplitudeIndicator
import com.hedvig.android.odyssey.R
import com.hedvig.android.odyssey.model.AudioUrl
import com.hedvig.android.odyssey.navigation.AudioContent
import com.hedvig.android.odyssey.step.audiorecording.AudioRecordingUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import java.io.File

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
    AudioRecordingUiState.NotRecording -> NotRecording(
      startRecording = startRecording,
      modifier = modifier,
    )
    is AudioRecordingUiState.Recording -> Recording(
      uiState = uiState,
      stopRecording = stopRecording,
      clock = clock,
      modifier = modifier,
    )
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
  }
}

@Composable
private fun NotRecording(
  startRecording: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
  ) {
    IconButton(
      onClick = startRecording,
      modifier = Modifier
        .padding(bottom = 24.dp)
        .then(Modifier.size(72.dp)),
    ) {
      Image(
        painter = painterResource(R.drawable.ic_record),
        contentDescription = stringResource(hedvig.resources.R.string.EMBARK_START_RECORDING),
      )
    }
    Text(
      text = stringResource(hedvig.resources.R.string.EMBARK_START_RECORDING),
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.padding(bottom = 16.dp),
    )
  }
}

@Composable
private fun Recording(
  uiState: AudioRecordingUiState.Recording,
  stopRecording: () -> Unit,
  clock: Clock,
  modifier: Modifier = Modifier,
) {
  ScreenOnFlag()
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .padding(bottom = 24.dp),
    ) {
      if (uiState.amplitudes.isNotEmpty()) {
        RecordingAmplitudeIndicator(amplitude = uiState.amplitudes.last())
      }
      IconButton(
        onClick = stopRecording,
        modifier = Modifier.size(72.dp),
      ) {
        Image(
          painter = painterResource(
            R.drawable.ic_record_stop,
          ),
          contentDescription = stringResource(hedvig.resources.R.string.EMBARK_STOP_RECORDING),
        )
      }
    }
    val diff = clock.now() - uiState.startedAt
    val label = String.format("%02d:%02d", diff.inWholeMinutes, diff.inWholeSeconds % 60)
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.padding(bottom = 16.dp),
    )
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

    LargeContainedTextButton(
      onClick = submit,
      text = stringResource(hedvig.resources.R.string.general_continue_button),
      enabled = uiState.canSubmit,
      modifier = Modifier.padding(top = 16.dp),
    )

    LargeTextButton(
      onClick = redo,
      enabled = uiState.canSubmit,
      modifier = Modifier.padding(top = 8.dp),
    ) {
      Text(stringResource(hedvig.resources.R.string.EMBARK_RECORD_AGAIN))
    }
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
        LargeContainedTextButton(
          onClick = submitAudioUrl,
          text = stringResource(hedvig.resources.R.string.general_continue_button),
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
        LargeTextButton(
          onClick = redo,
          enabled = uiState.canSubmit,
        ) {
          Text(stringResource(hedvig.resources.R.string.EMBARK_RECORD_AGAIN))
        }
      }
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
          override val audioPlayerState: StateFlow<AudioPlayerState> = MutableStateFlow(AudioPlayerState.Ready.done())
          override fun initialize() = TODO("Not yet implemented")
          override fun startPlayer() = TODO("Not yet implemented")
          override fun pausePlayer() = TODO("Not yet implemented")
          override fun retryLoadingAudio() = TODO("Not yet implemented")
          override fun seekTo(progressPercentage: ProgressPercentage) = TODO("Not yet implemented")
          override fun close() = TODO("Not yet implemented")
        },
      )
    }
  }
}
