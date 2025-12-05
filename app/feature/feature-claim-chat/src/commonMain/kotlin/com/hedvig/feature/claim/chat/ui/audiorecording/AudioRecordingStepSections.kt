package com.hedvig.feature.claim.chat.ui.audiorecording

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.PermissionDialog
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplay
import com.hedvig.feature.claim.chat.data.AudioRecordingStepState
import com.hedvig.feature.claim.chat.data.FreeTextErrorType
import hedvig.resources.CHAT_UPLOAD_PRESS_SEND_LABEL
import hedvig.resources.CLAIMS_TEXT_INPUT_MIN_CHARACTERS_ERROR
import hedvig.resources.CLAIMS_TEXT_INPUT_PLACEHOLDER
import hedvig.resources.CLAIMS_USE_AUDIO_RECORDING
import hedvig.resources.PERMISSION_DIALOG_RECORD_AUDIO_MESSAGE
import hedvig.resources.Res
import hedvig.resources.claims_skip_button
import kotlin.time.Clock
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AudioRecordingStep(
  uiState: AudioRecordingStepState,
  freeText: String?,
  clock: Clock,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  startRecording: () -> Unit,
  stopRecording: () -> Unit,
  submitAudioFile: () -> Unit,
  redo: () -> Unit,
  openAppSettings: () -> Unit,
  freeTextAvailable: Boolean,
  submitFreeText: () -> Unit,
  showFreeText: () -> Unit,
  showAudioRecording: () -> Unit,
  onLaunchFullScreenEditText: () -> Unit,
  canSkip: Boolean,
  onSkip: () -> Unit,
  isCurrentStep: Boolean,
  modifier: Modifier = Modifier,
) {
  AnimatedContent(
    uiState,
    contentKey = { s ->
      when (s) {
        is AudioRecordingStepState.AudioRecording -> "audio_recording"
        is AudioRecordingStepState.FreeTextDescription -> "freetext"
      }
    },
  ) { uiStateAnimated ->
    Column(modifier) {
      when (uiStateAnimated) {
        is AudioRecordingStepState.AudioRecording -> {
          AudioRecordingSection(
            uiState = uiStateAnimated,
            clock = clock,
            shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale,
            startRecording = startRecording,
            stopRecording = stopRecording,
            submitAudioFile = submitAudioFile,
            redo = redo,
            openAppSettings = openAppSettings,
            allowFreeText = freeTextAvailable,
            launchFreeText = showFreeText,
            isCurrentStep = isCurrentStep,
          )
        }

        is AudioRecordingStepState.FreeTextDescription -> {
          FreeTextInputSection(
            submitFreeText = submitFreeText,
            showAudioRecording = showAudioRecording,
            onLaunchFullScreenEditText = onLaunchFullScreenEditText,
            freeText = freeText,
            hasError = uiStateAnimated.hasError,
            errorType = uiStateAnimated.errorType,
            isCurrentStep = isCurrentStep,
          )
        }
      }
      if (canSkip) {
        HedvigTextButton(
          stringResource(Res.string.claims_skip_button),
          onClick = onSkip,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.width(16.dp))
      }
    }
  }
}

@Composable
private fun FreeTextInputSection(
  freeText: String?,
  showAudioRecording: () -> Unit,
  onLaunchFullScreenEditText: () -> Unit,
  submitFreeText: () -> Unit,
  hasError: Boolean,
  isCurrentStep: Boolean,
  errorType: FreeTextErrorType?,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier,
    horizontalAlignment = Alignment.End,
  ) {
    FreeTextDisplay(
      onClick = { onLaunchFullScreenEditText() },
      freeTextValue = freeText,
      freeTextPlaceholder = stringResource(Res.string.CLAIMS_TEXT_INPUT_PLACEHOLDER),
      supportingText = if (errorType is FreeTextErrorType.TooShort) {
        stringResource(Res.string.CLAIMS_TEXT_INPUT_MIN_CHARACTERS_ERROR, errorType.minLength)
      } else {
        null
      },
      hasError = hasError,
    )
    if (isCurrentStep) {
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        onClick = submitFreeText,
        enabled = true,
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(Res.string.CHAT_UPLOAD_PRESS_SEND_LABEL),
      )
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = stringResource(Res.string.CLAIMS_USE_AUDIO_RECORDING),
        onClick = showAudioRecording,
        modifier = Modifier.fillMaxWidth(),
        enabled = true,
      )
    }
  }
}

@Composable
private fun AudioRecordingSection(
  uiState: AudioRecordingStepState.AudioRecording,
  clock: Clock,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  startRecording: () -> Unit,
  stopRecording: () -> Unit,
  submitAudioFile: () -> Unit,
  redo: () -> Unit,
  openAppSettings: () -> Unit,
  launchFreeText: () -> Unit,
  allowFreeText: Boolean,
  isCurrentStep: Boolean,
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
    rememberPermissionState(RECORD_AUDIO_PERMISSION) { isGranted ->
      if (isGranted) {
        startRecording()
      } else {
        showPermissionDialog = true
      }
    }
  }
  if (showPermissionDialog) {
    PermissionDialog(
      permissionDescription = stringResource(Res.string.PERMISSION_DIALOG_RECORD_AUDIO_MESSAGE),
      isPermanentlyDeclined = !shouldShowRequestPermissionRationale(RECORD_AUDIO_PERMISSION),
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
    redo = redo,
    modifier = modifier,
    allowFreeText = allowFreeText,
    onLaunchFreeText = launchFreeText,
    isCurrentStep = isCurrentStep,
  )
}

@HedvigPreview
@Composable
private fun PreviewFreeTextInput() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      FreeTextInputSection(
        "some free text",
        {},
        {},
        {},
        hasError = false,
        errorType = null,
        isCurrentStep = true,
      )
    }
  }
}

// Platform-specific permission constant
internal expect val RECORD_AUDIO_PERMISSION: String
