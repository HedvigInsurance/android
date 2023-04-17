package com.hedvig.android.odyssey.step.audiorecording

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.hedvig.android.core.designsystem.component.button.LargeContainedTextButton
import com.hedvig.android.core.designsystem.component.button.LargeTextButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.card.HedvigCardElevation
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.ui.ClaimFlowScaffold
import com.hedvig.odyssey.renderers.audiorecorder.PlaybackWaveForm
import com.hedvig.odyssey.renderers.audiorecorder.RecordingAmplitudeIndicator
import com.hedvig.odyssey.renderers.utils.ScreenOnFlag
import hedvig.resources.R
import java.io.File
import kotlinx.datetime.Clock

@Composable
internal fun AudioRecordingDestination(
  viewModel: AudioRecordingViewModel,
  windowSizeClass: WindowSizeClass,
  questions: List<String>,
  openAppSettings: () -> Unit,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val claimFlowStep = (uiState as? AudioRecordingUiState.Playback)?.nextStep
  LaunchedEffect(claimFlowStep) {
    if (claimFlowStep != null) {
      navigateToNextStep(claimFlowStep)
    }
  }
  AudioRecordingScreen(
    uiState = uiState,
    windowSizeClass = windowSizeClass,
    questions = questions,
    clock = viewModel.clock,
    startRecording = viewModel::startRecording,
    stopRecording = viewModel::stopRecording,
    submitAudioFile = viewModel::submitAudioFile,
    redo = viewModel::redo,
    play = viewModel::play,
    pause = viewModel::pause,
    showedError = viewModel::showedError,
    openAppSettings = openAppSettings,
    navigateUp = navigateUp,
  )
}

@Composable
private fun AudioRecordingScreen(
  uiState: AudioRecordingUiState,
  windowSizeClass: WindowSizeClass,
  questions: List<String>,
  clock: Clock,
  startRecording: () -> Unit,
  stopRecording: () -> Unit,
  submitAudioFile: (File) -> Unit,
  redo: () -> Unit,
  play: () -> Unit,
  pause: () -> Unit,
  showedError: () -> Unit,
  openAppSettings: () -> Unit,
  navigateUp: () -> Unit,
) {
  ClaimFlowScaffold(
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    topAppBarText = stringResource(R.string.claims_incident_screen_header),
    isLoading = false,
    errorSnackbarState = ErrorSnackbarState(uiState.hasAudioSubmissionError, showedError),
  ) { sideSpacingModifier ->
    Spacer(Modifier.height(20.dp))
    for (question in questions) {
      HedvigCard(
        shape = RoundedCornerShape(12.dp),
        elevation = HedvigCardElevation.Elevated(),
        modifier = sideSpacingModifier.padding(end = 16.dp),
      ) {
        Text(
          text = question,
          modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
          style = MaterialTheme.typography.bodyLarge,
        )
      }
      Spacer(Modifier.height(8.dp))
    }
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    AudioRecordingSection(
      uiState = uiState,
      clock = clock,
      startRecording = startRecording,
      stopRecording = stopRecording,
      submitAudioFile = submitAudioFile,
      redo = redo,
      play = play,
      pause = pause,
      openAppSettings = openAppSettings,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(
      Modifier.windowInsetsPadding(
        WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
      ),
    )
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun AudioRecordingSection(
  uiState: AudioRecordingUiState,
  clock: Clock,
  startRecording: () -> Unit,
  stopRecording: () -> Unit,
  submitAudioFile: (File) -> Unit,
  redo: () -> Unit,
  play: () -> Unit,
  pause: () -> Unit,
  openAppSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
  var openPermissionDialog by rememberSaveable { mutableStateOf(false) }

  if (openPermissionDialog) {
    PermissionDialog(
      recordAudioPermissionState = recordAudioPermissionState,
      dismiss = { openPermissionDialog = false },
    )
  }

  AudioRecorder(
    uiState = uiState,
    audioPermissionStatus = recordAudioPermissionState.status,
    startRecording = {
      when (recordAudioPermissionState.status) {
        PermissionStatus.Granted -> startRecording()
        is PermissionStatus.Denied -> openPermissionDialog = true
      }
    },
    clock = clock,
    stopRecording = stopRecording,
    submitAudioFile = submitAudioFile,
    redo = redo,
    play = play,
    pause = pause,
    openAppSettings = openAppSettings,
    modifier = modifier,
  )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun AudioRecorder(
  uiState: AudioRecordingUiState,
  audioPermissionStatus: PermissionStatus,
  startRecording: () -> Unit,
  clock: Clock,
  stopRecording: () -> Unit,
  submitAudioFile: (File) -> Unit,
  redo: () -> Unit,
  play: () -> Unit,
  pause: () -> Unit,
  openAppSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  when (audioPermissionStatus) {
    is PermissionStatus.Denied -> {
      DeniedMicrophonePermission(modifier, openAppSettings)
    }
    PermissionStatus.Granted -> {
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
          play = play,
          pause = pause,
          modifier = modifier,
        )
      }
    }
  }
}

@Composable
private fun DeniedMicrophonePermission(
  modifier: Modifier = Modifier,
  openAppSettings: () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
  ) {
    Text(stringResource(R.string.PERMISSION_DIALOG_RECORD_AUDIO_MESSAGE))
    Spacer(Modifier.height(16.dp))
    LargeContainedTextButton(
      text = stringResource(R.string.SETTINGS_TITLE),
      onClick = { openAppSettings() },
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
        painter = painterResource(com.hedvig.android.odyssey.R.drawable.ic_record),
        contentDescription = stringResource(R.string.EMBARK_START_RECORDING),
      )
    }
    Text(
      text = stringResource(R.string.EMBARK_START_RECORDING),
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
            com.hedvig.android.odyssey.R.drawable.ic_record_stop,
          ),
          contentDescription = stringResource(R.string.EMBARK_STOP_RECORDING),
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
  play: () -> Unit,
  pause: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
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
      text = stringResource(R.string.general_continue_button),
      enabled = uiState.canSubmit,
      modifier = Modifier.padding(top = 16.dp),
    )

    LargeTextButton(
      onClick = redo,
      enabled = uiState.canSubmit,
      modifier = Modifier.padding(top = 8.dp),
    ) {
      Text(stringResource(R.string.EMBARK_RECORD_AGAIN))
    }
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun PermissionDialog(
  recordAudioPermissionState: PermissionState,
  dismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = dismiss,
    title = { Text(stringResource(R.string.PERMISSION_DIALOG_TITLE)) },
    text = { Text(stringResource(R.string.PERMISSION_DIALOG_RECORD_AUDIO_MESSAGE)) },
    dismissButton = {
      TextButton(
        onClick = dismiss,
        shape = MaterialTheme.shapes.medium,
      ) {
        Text(stringResource(android.R.string.cancel))
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          dismiss()
          recordAudioPermissionState.launchPermissionRequest()
        },
        shape = MaterialTheme.shapes.medium,
      ) {
        Text(stringResource(android.R.string.ok))
      }
    },
  )
}
