package com.hedvig.android.odyssey.step.audiorecording

import android.Manifest
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.card.HedvigCardElevation
import com.hedvig.android.core.ui.permission.PermissionDialog
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.model.AudioUrl
import com.hedvig.android.odyssey.step.audiorecording.ui.AudioRecorder
import com.hedvig.android.odyssey.ui.ClaimFlowScaffold
import hedvig.resources.R
import kotlinx.datetime.Clock
import java.io.File

@Composable
internal fun AudioRecordingDestination(
  viewModel: AudioRecordingViewModel,
  windowSizeClass: WindowSizeClass,
  questions: List<String>,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openAppSettings: () -> Unit,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val claimFlowStep = uiState.nextStep
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
    shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
    startRecording = viewModel::startRecording,
    stopRecording = viewModel::stopRecording,
    submitAudioFile = viewModel::submitAudioFile,
    submitAudioUrl = viewModel::submitAudioUrl,
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
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  startRecording: () -> Unit,
  stopRecording: () -> Unit,
  submitAudioFile: (File) -> Unit,
  submitAudioUrl: (AudioUrl) -> Unit,
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
    isLoading = uiState.isLoading,
    errorSnackbarState = ErrorSnackbarState(uiState.hasError, showedError),
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
      shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
      startRecording = startRecording,
      stopRecording = stopRecording,
      submitAudioFile = submitAudioFile,
      submitAudioUrl = submitAudioUrl,
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
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  startRecording: () -> Unit,
  stopRecording: () -> Unit,
  submitAudioFile: (File) -> Unit,
  submitAudioUrl: (AudioUrl) -> Unit,
  redo: () -> Unit,
  play: () -> Unit,
  pause: () -> Unit,
  openAppSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showPermissionDialog by remember { mutableStateOf(false) }
  val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO) { isGranted ->
    if (isGranted) {
      startRecording()
    } else {
      showPermissionDialog = true
    }
  }
  if (showPermissionDialog) {
    PermissionDialog(
      permissionDescription = stringResource(R.string.PERMISSION_DIALOG_RECORD_AUDIO_MESSAGE),
      isPermanentlyDeclined = !shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO),
      onDismiss = { showPermissionDialog = false },
      okClick = recordAudioPermissionState::launchPermissionRequest,
      openAppSettings = openAppSettings,
    )
  }

  AudioRecorder(
    uiState = uiState,
    startRecording = recordAudioPermissionState::launchPermissionRequest,
    clock = clock,
    stopRecording = stopRecording,
    submitAudioFile = submitAudioFile,
    submitAudioUrl = submitAudioUrl,
    redo = redo,
    play = play,
    pause = pause,
    modifier = modifier,
  )
}
