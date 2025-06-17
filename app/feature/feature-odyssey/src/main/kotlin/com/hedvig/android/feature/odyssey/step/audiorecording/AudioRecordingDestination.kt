package com.hedvig.android.feature.odyssey.step.audiorecording

import android.Manifest
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.model.AudioUrl
import com.hedvig.android.design.system.hedvig.ErrorSnackbarState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.PermissionDialog
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.calculateForPreview
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplay
import com.hedvig.android.design.system.hedvig.freetext.FreeTextOverlay
import com.hedvig.android.feature.odyssey.step.audiorecording.WhatHappenedUiState.AudioRecording.NotRecording
import com.hedvig.android.feature.odyssey.step.audiorecording.WhatHappenedUiState.ScreenMode
import com.hedvig.android.feature.odyssey.step.audiorecording.ui.AudioRecorder
import com.hedvig.android.ui.claimflow.ClaimFlowScaffold
import hedvig.resources.R
import java.io.File
import kotlinx.datetime.Clock

@Composable
internal fun AudioRecordingDestination(
  viewModel: AudioRecordingViewModel,
  windowSizeClass: WindowSizeClass,
  questions: List<String>,
  freeTextQuestions: List<String>,
  freeTextAvailable: Boolean,
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
    submitFreeText = viewModel::submitFreeText,
    freeTextQuestions = freeTextQuestions,
    freeTextAvailable = freeTextAvailable,
    showFreeText = {
      viewModel.switchMode(ScreenMode.FREE_TEXT)
    },
    showAudioRecording = {
      viewModel.switchMode(ScreenMode.RECORDING)
    },
    updateFreeText = viewModel::updateFreeText,
    onLaunchFullScreenEditText = viewModel::onLaunchFullScreenOverlay,
    onCloseFullScreenEditText = viewModel::onCloseFullScreenOverlay,
  )
}

@Composable
private fun AudioRecordingScreen(
  uiState: WhatHappenedUiState,
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
  freeTextQuestions: List<String>,
  freeTextAvailable: Boolean,
  submitFreeText: () -> Unit,
  showFreeText: () -> Unit,
  showAudioRecording: () -> Unit,
  updateFreeText: (String?) -> Unit,
  onCloseFullScreenEditText: () -> Unit,
  onLaunchFullScreenEditText: () -> Unit,
) {
  FreeTextOverlay(
    freeTextMaxLength = 2000,
    freeTextValue = if (uiState is WhatHappenedUiState.FreeTextDescription) uiState.freeText else null,
    freeTextHint = stringResource(id = R.string.CLAIMS_TEXT_INPUT_PLACEHOLDER),
    freeTextOnCancelClick = {
      onCloseFullScreenEditText()
    },
    freeTextOnSaveClick = { feedback ->
      updateFreeText(feedback)

      onCloseFullScreenEditText()
    },
    shouldShowOverlay = if (uiState is WhatHappenedUiState.FreeTextDescription) uiState.showOverlay else false,
    overlaidContent = {
      ClaimFlowScaffold(
        windowSizeClass = windowSizeClass,
        navigateUp = navigateUp,
        closeClaimFlow = closeClaimFlow,
        modifier = Modifier.fillMaxSize(),
        errorSnackbarState = ErrorSnackbarState(uiState.hasError, showedError),
      ) { sideSpacingModifier ->
        Spacer(Modifier.height(16.dp))
        AnimatedContent(
          uiState,
          contentKey = { s ->
            when (s) {
              is WhatHappenedUiState.AudioRecording -> "audio_recording"
              is WhatHappenedUiState.FreeTextDescription -> "freetext"
            }
          },
        ) { uiStateAnimated ->
          Column {
            val suitableQuestions = when (uiStateAnimated) {
              is WhatHappenedUiState.AudioRecording -> questions
              is WhatHappenedUiState.FreeTextDescription -> freeTextQuestions
            }
            for (question in suitableQuestions) {
              HedvigCard(modifier = sideSpacingModifier.padding(end = 16.dp)) {
                HedvigText(
                  text = question,
                  modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                )
              }
              Spacer(Modifier.height(8.dp))
            }
          }
        }
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        AnimatedContent(
          uiState,
          contentKey = { s ->
            when (s) {
              is WhatHappenedUiState.AudioRecording -> "audio_recording"
              is WhatHappenedUiState.FreeTextDescription -> "freetext"
            }
          },
        ) { uiStateAnimated ->
          Column {
            when (uiStateAnimated) {
              is WhatHappenedUiState.AudioRecording -> {
                AudioRecordingSection(
                  uiState = uiStateAnimated,
                  clock = clock,
                  shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
                  startRecording = startRecording,
                  stopRecording = stopRecording,
                  submitAudioFile = submitAudioFile,
                  submitAudioUrl = submitAudioUrl,
                  redo = redo,
                  openAppSettings = openAppSettings,
                  modifier = sideSpacingModifier,
                  allowFreeText = freeTextAvailable,
                  launchFreeText = showFreeText,
                )
              }

              is WhatHappenedUiState.FreeTextDescription -> {
                FreeTextInputSection(
                  submitFreeText = submitFreeText,
                  showAudioRecording = showAudioRecording,
                  uiState = uiStateAnimated,
                  modifier = sideSpacingModifier,
                  onLaunchFullScreenEditText = onLaunchFullScreenEditText,
                )
              }
            }
          }
        }
        Spacer(Modifier.height(16.dp))
        Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
      }
    },
  )
}

@Composable
private fun FreeTextInputSection(
  uiState: WhatHappenedUiState.FreeTextDescription,
  showAudioRecording: () -> Unit,
  onLaunchFullScreenEditText: () -> Unit,
  submitFreeText: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier.fillMaxWidth()) {
    FreeTextDisplay(
      onClick = { onLaunchFullScreenEditText() },
      freeTextValue = uiState.freeText,
      freeTextPlaceholder = stringResource(id = R.string.CLAIMS_TEXT_INPUT_PLACEHOLDER),
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      onClick = submitFreeText,
      text = stringResource(R.string.general_continue_button),
      isLoading = uiState.isLoading,
      enabled = uiState.canSubmit,
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(R.string.CLAIMS_USE_AUDIO_RECORDING),
      onClick = showAudioRecording,
      enabled = true,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun AudioRecordingSection(
  uiState: WhatHappenedUiState.AudioRecording,
  clock: Clock,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  startRecording: () -> Unit,
  stopRecording: () -> Unit,
  submitAudioFile: (File) -> Unit,
  submitAudioUrl: (AudioUrl) -> Unit,
  redo: () -> Unit,
  openAppSettings: () -> Unit,
  launchFreeText: () -> Unit,
  allowFreeText: Boolean,
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
    allowFreeText = allowFreeText,
    onLaunchFreeText = launchFreeText,
  )
}

@HedvigPreview
@Composable
private fun PreviewAudioRecordingScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AudioRecordingScreen(
        NotRecording,
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
        listOf(
          "Free txt question 1",
          "Free txt question 2",
          "Free txt question 3",
        ),
        freeTextAvailable = true,
        submitFreeText = {},
        showAudioRecording = {},
        showFreeText = {},
        updateFreeText = {},
        onCloseFullScreenEditText = {},
        onLaunchFullScreenEditText = {},
      )
    }
  }
}
