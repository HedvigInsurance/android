package com.hedvig.android.odyssey.step.audiorecording

import android.Manifest
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
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
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.component.button.LargeTextButton
import com.hedvig.android.odyssey.repository.AutomationClaimInputDTO2
import com.hedvig.odyssey.renderers.audiorecorder.PlaybackWaveForm
import com.hedvig.odyssey.renderers.audiorecorder.RecordingAmplitudeIndicator
import com.hedvig.odyssey.renderers.utils.ScreenOnFlag
import hedvig.resources.R
import kotlinx.datetime.Clock
import java.io.File

@Composable
internal fun AudioRecordingDestination(
  viewModel: AudioRecordingViewModel,
  questions: List<AutomationClaimInputDTO2.AudioRecording.AudioRecordingQuestion>,
) {
  val uiState by viewModel.uiState.collectAsState()
  AudioRecordingScreen(
    uiState = uiState,
    questions = questions,
    clock = viewModel.clock,
    startRecording = viewModel::startRecording,
    stopRecording = viewModel::stopRecording,
    submitAudioFile = viewModel::submitAudioFile,
    redo = viewModel::redo,
    play = viewModel::play,
    pause = viewModel::pause,
  )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun AudioRecordingScreen(
  uiState: AudioRecordingUiState,
  questions: List<AutomationClaimInputDTO2.AudioRecording.AudioRecordingQuestion>,
  clock: Clock,
  startRecording: () -> Unit,
  stopRecording: () -> Unit,
  submitAudioFile: (File) -> Unit,
  redo: () -> Unit,
  play: () -> Unit,
  pause: () -> Unit,
) {
  var openPermissionDialog by remember { mutableStateOf(false) }
  val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)

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
        uiState = uiState,
        startRecording = {
          when (recordAudioPermissionState.status) {
            PermissionStatus.Granted -> startRecording()
            is PermissionStatus.Denied -> openPermissionDialog = true
          }
        },
        clock = clock,
        stopRecording = stopRecording,
        submit = {
          // TODO file handling
          val filePath = (uiState as? AudioRecordingUiState.Playback)!!.filePath
          val audioFile = File(filePath)
          submitAudioFile(audioFile)
        },
        redo = redo,
        play = play,
        pause = pause,
        startRecordingText = stringResource(R.string.EMBARK_START_RECORDING),
        stopRecordingText = stringResource(R.string.EMBARK_STOP_RECORDING),
        recordAgainText = stringResource(R.string.EMBARK_RECORD_AGAIN),
        submitClaimText = stringResource(R.string.general_continue_button),
      )
    }
  }

  if (openPermissionDialog) {
    PermissionDialog(
      recordAudioPermissionState = recordAudioPermissionState,
      permissionTitle = stringResource(R.string.PERMISSION_DIALOG_TITLE),
      permissionMessage = stringResource(R.string.PERMISSION_DIALOG_RECORD_AUDIO_MESSAGE),
      dismiss = { openPermissionDialog = false },
    )
  }
}

@Composable
private fun AudioRecorder(
  uiState: AudioRecordingUiState,
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
  when (uiState) {
    AudioRecordingUiState.NotRecording -> NotRecording(
      startRecording = startRecording,
      startRecordingText = startRecordingText,
    )

    is AudioRecordingUiState.Recording -> Recording(
      uiState = uiState,
      stopRecording = stopRecording,
      stopRecordingText = stopRecordingText,
      clock = clock,
    )

    is AudioRecordingUiState.Playback -> Playback(
      uiState = uiState,
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
private fun NotRecording(startRecording: () -> Unit, startRecordingText: String) {
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
          com.hedvig.android.odyssey.R.drawable.ic_record,
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
private fun Recording(
  uiState: AudioRecordingUiState.Recording,
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
      if (uiState.amplitudes.isNotEmpty()) {
        RecordingAmplitudeIndicator(amplitude = uiState.amplitudes.last())
      }
      IconButton(
        onClick = stopRecording,
        modifier = Modifier.size(72.dp),
      ) {
        Image(
          painter = painterResource(
            com.hedvig.android.odyssey.R.drawable.ic_record_stop,
          ),
          contentDescription = stopRecordingText,
        )
      }
    }
    val diff = clock.now() - uiState.startedAt
    val label = String.format("%02d:%02d", diff.inWholeMinutes, diff.inWholeSeconds % 60)
    Text(
      text = label,
      style = MaterialTheme.typography.caption,
      modifier = Modifier.padding(bottom = 16.dp),
    )
  }
}

@Composable
private fun Playback(
  uiState: AudioRecordingUiState.Playback,
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
    if (!uiState.isPrepared) {
      CircularProgressIndicator()
    } else {
      PlaybackWaveForm(
        isPlaying = uiState.isPlaying,
        play = play,
        pause = pause,
        amplitudes = uiState.amplitudes,
        progress = uiState.progress,
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionDialog(
  recordAudioPermissionState: PermissionState,
  permissionTitle: String,
  permissionMessage: String,
  dismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = dismiss,
    title = { androidx.compose.material3.Text(permissionTitle) },
    text = { androidx.compose.material3.Text(permissionMessage) },
    dismissButton = {
      TextButton(
        onClick = dismiss,
      ) {
        androidx.compose.material3.Text(stringResource(android.R.string.cancel))
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          dismiss()
          recordAudioPermissionState.launchPermissionRequest()
        },
      ) {
        androidx.compose.material3.Text(stringResource(android.R.string.ok))
      }
    },
  )
}
