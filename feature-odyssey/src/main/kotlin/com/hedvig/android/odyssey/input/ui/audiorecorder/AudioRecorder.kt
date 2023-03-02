package com.hedvig.android.odyssey.input.ui.audiorecorder

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.component.button.LargeTextButton
import com.hedvig.android.odyssey.R
import com.hedvig.android.odyssey.repository.AutomationClaimInputDTO2
import com.hedvig.odyssey.remote.file.File
import com.hedvig.odyssey.remote.file.FileContent
import com.hedvig.odyssey.renderers.audiorecorder.PlaybackWaveForm
import com.hedvig.odyssey.renderers.audiorecorder.RecordingAmplitudeIndicator
import com.hedvig.odyssey.renderers.utils.ScreenOnFlag
import com.hedvig.odyssey.utils.contentType
import java.time.Clock
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AudioRecorderScreen(
  questions: List<AutomationClaimInputDTO2.AudioRecording.AudioRecordingQuestion>,
  onAudioFile: (File) -> Unit,
  onNext: () -> Unit,
  audioRecorderViewModel: AudioRecorderViewModel,
) {
  val audioRecorderViewState by audioRecorderViewModel.viewState.collectAsState()

  var openPermissionDialog by remember { mutableStateOf(false) }
  val recordAudioPermissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)

  Box(
    Modifier
      .fillMaxHeight()
      .padding(all = 16.dp),
  ) {
    Column {
      questions.forEach {
        Surface(
          elevation = 2.dp,
          modifier = Modifier.padding(vertical = 4.dp),
          shape = RoundedCornerShape(20),
        ) {
          Text(
            text = it.getText(),
            modifier = Modifier.padding(12.dp),
            fontSize = 16.sp,
          )
        }
      }
    }

    Box(Modifier.align(Alignment.BottomCenter)) {
      AudioRecorder(
        viewState = audioRecorderViewState,
        startRecording = {
          when (recordAudioPermissionState.status) {
            PermissionStatus.Granted -> audioRecorderViewModel.startRecording()
            is PermissionStatus.Denied -> openPermissionDialog = true
          }
        },
        clock = audioRecorderViewModel.clock,
        stopRecording = audioRecorderViewModel::stopRecording,
        submit = {
          // TODO
          val filePath = (audioRecorderViewState as? AudioRecorderViewModel.ViewState.Playback)!!.filePath
          val audioFile = File(
            name = "AudioRecording",
            content = FileContent(path = filePath),
            contentType = filePath.contentType(),
          )
          onAudioFile(audioFile)
          onNext()
        },
        redo = audioRecorderViewModel::redo,
        play = audioRecorderViewModel::play,
        pause = audioRecorderViewModel::pause,
        startRecordingText = stringResource(hedvig.resources.R.string.EMBARK_START_RECORDING),
        stopRecordingText = stringResource(hedvig.resources.R.string.EMBARK_STOP_RECORDING),
        recordAgainText = stringResource(hedvig.resources.R.string.EMBARK_RECORD_AGAIN),
        submitClaimText = stringResource(hedvig.resources.R.string.general_continue_button),
      )
    }
  }

  if (openPermissionDialog) {
    PermissionDialog(
      recordAudioPermissionState = recordAudioPermissionState,
      permissionTitle = stringResource(hedvig.resources.R.string.PERMISSION_DIALOG_TITLE),
      permissionMessage = stringResource(hedvig.resources.R.string.PERMISSION_DIALOG_RECORD_AUDIO_MESSAGE),
      dismiss = { openPermissionDialog = false },
    )
  }
}

@Composable
fun AudioRecorder(
  viewState: AudioRecorderViewModel.ViewState,
  startRecording: () -> Unit,
  clock: Clock,
  stopRecording: () -> Unit,
  submit: () -> Unit,
  redo: () -> Unit,
  play: () -> Unit,
  pause: () -> Unit,
  startRecordingText: String,
  stopRecordingText: String,
  recordAgainText: String,
  submitClaimText: String,
) {
  when (viewState) {
    AudioRecorderViewModel.ViewState.NotRecording -> NotRecording(
      startRecording = startRecording,
      startRecordingText = startRecordingText,
    )

    is AudioRecorderViewModel.ViewState.Recording -> Recording(
      viewState = viewState,
      stopRecording = stopRecording,
      stopRecordingText = stopRecordingText,
      clock = clock,
    )

    is AudioRecorderViewModel.ViewState.Playback -> Playback(
      viewState = viewState,
      submit = submit,
      submitClaimText = submitClaimText,
      recordAgainText = recordAgainText,
      redo = redo,
      play = play,
      pause = pause,
    )
  }
}

@Composable
fun NotRecording(startRecording: () -> Unit, startRecordingText: String) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth(),
  ) {
    IconButton(
      onClick = startRecording,
      modifier = Modifier
        .padding(bottom = 24.dp)
        .then(Modifier.size(72.dp)),
    ) {
      Image(
        painter = painterResource(
          R.drawable.ic_record,
        ),
        contentDescription = startRecordingText,
      )
    }
    Text(
      text = startRecordingText,
      style = MaterialTheme.typography.caption,
      modifier = Modifier.padding(bottom = 16.dp),
    )
  }
}

@Composable
fun Recording(
  viewState: AudioRecorderViewModel.ViewState.Recording,
  stopRecording: () -> Unit,
  stopRecordingText: String,
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
      if (viewState.amplitudes.isNotEmpty()) {
        RecordingAmplitudeIndicator(amplitude = viewState.amplitudes.last())
      }
      IconButton(
        onClick = stopRecording,
        modifier = Modifier.size(72.dp),
      ) {
        Image(
          painter = painterResource(
            R.drawable.ic_record_stop,
          ),
          contentDescription = stopRecordingText,
        )
      }
    }
    val diff = Duration.between(
      viewState.startedAt,
      Instant.now(clock),
    )
    val label = String.format("%02d:%02d", diff.toMinutes(), diff.seconds % 60)
    Text(
      text = label,
      style = MaterialTheme.typography.caption,
      modifier = Modifier.padding(bottom = 16.dp),
    )
  }
}

@Composable
fun Playback(
  viewState: AudioRecorderViewModel.ViewState.Playback,
  submit: () -> Unit,
  submitClaimText: String,
  recordAgainText: String,
  redo: () -> Unit,
  play: () -> Unit,
  pause: () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth(),
  ) {
    if (!viewState.isPrepared) {
      CircularProgressIndicator()
    } else {
      PlaybackWaveForm(
        isPlaying = viewState.isPlaying,
        play = play,
        pause = pause,
        amplitudes = viewState.amplitudes,
        progress = viewState.progress,
      )
    }

    LargeContainedTextButton(
      onClick = submit,
      text = submitClaimText,
      modifier = Modifier.padding(top = 16.dp),
    )

    LargeTextButton(
      onClick = redo,
      modifier = Modifier.padding(top = 8.dp),
    ) {
      Text(recordAgainText)
    }
  }
}
