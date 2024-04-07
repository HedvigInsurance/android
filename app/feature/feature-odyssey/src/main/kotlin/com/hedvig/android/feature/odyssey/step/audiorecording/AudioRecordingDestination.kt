package com.hedvig.android.feature.odyssey.step.audiorecording

import android.Manifest
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.hedvig.android.core.designsystem.HedvigPreviewLayout
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.card.HedvigCardElevation
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.ui.permission.PermissionDialog
import com.hedvig.android.core.ui.preview.calculateForPreview
import com.hedvig.android.core.ui.scaffold.ClaimFlowScaffold
import com.hedvig.android.core.ui.snackbar.ErrorSnackbarState
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.model.AudioUrl
import com.hedvig.android.feature.odyssey.step.audiorecording.ui.AudioRecorder
import hedvig.resources.R
import java.io.File
import kotlinx.datetime.Clock

@Composable
internal fun SharedTransitionScope.AudioRecordingDestination(
  animatedContentScope: AnimatedContentScope,
  viewModel: AudioRecordingViewModel,
  windowSizeClass: WindowSizeClass,
  questions: List<String>,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  openAppSettings: () -> Unit,
  navigateToNextStep: (ClaimFlowStep) -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val claimFlowStep = uiState.nextStep
  LaunchedEffect(claimFlowStep) {
    if (claimFlowStep != null) {
      navigateToNextStep(claimFlowStep)
    }
  }
  AudioRecordingScreen(
    animatedContentScope = animatedContentScope,
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
    showedError = viewModel::showedError,
    openAppSettings = openAppSettings,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
  )
}

@Composable
private fun SharedTransitionScope.AudioRecordingScreen(
  animatedContentScope: AnimatedContentScope,
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
  showedError: () -> Unit,
  openAppSettings: () -> Unit,
  navigateUp: () -> Unit,
  closeClaimFlow: () -> Unit,
) {
  ClaimFlowScaffold(
    animatedContentScope = animatedContentScope,
    windowSizeClass = windowSizeClass,
    navigateUp = navigateUp,
    closeClaimFlow = closeClaimFlow,
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
      openAppSettings = openAppSettings,
      modifier = sideSpacingModifier,
    )
    Spacer(Modifier.height(16.dp))
    Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
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
  openAppSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showPermissionDialog by remember { mutableStateOf(false) }
  val recordAudioPermissionState = if (LocalInspectionMode.current) {
    object : PermissionState {
      override val permission: String = ""
      override val status: PermissionStatus = PermissionStatus.Granted

      override fun launchPermissionRequest() {}
    }
  } else {
    rememberPermissionState(Manifest.permission.RECORD_AUDIO) { isGranted ->
      if (isGranted) {
        startRecording()
      } else {
        showPermissionDialog = true
      }
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
    modifier = modifier,
  )
}

@HedvigPreview
@Composable
private fun PreviewAudioRecordingScreen() {
  HedvigPreviewLayout { animatedContentScope ->
    AudioRecordingScreen(
      animatedContentScope,
      AudioRecordingUiState.NotRecording,
      WindowSizeClass.calculateForPreview(),
      listOf(
        "Perfect, now you need to make a voice recording. Try and answer the questions with as much detail as",
        "What happened?",
        "How did it happen?",
      ),
      Clock.System,
      { false },
      {},
      {},
      {},
      {},
      {},
      {},
      {},
      {},
      {},
    )
  }
}
