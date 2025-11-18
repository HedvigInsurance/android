package com.hedvig.feature.claim.chat.ui.audiorecording

import android.Manifest
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.PermissionDialog
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplay
import com.hedvig.android.design.system.hedvig.freetext.FreeTextOverlay
import hedvig.resources.R
import java.io.File
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.serialization.Serializable

internal sealed interface AudioRecordingStepState {
  data class FreeTextDescription(
    val freeText: String?,
    val showOverlay: Boolean,
    val errorType: FreeTextErrorType?,
    val hasError: Boolean = false,
  ) : AudioRecordingStepState

  sealed interface AudioRecording : AudioRecordingStepState {

    data object NotRecording : AudioRecording

    data class Recording(
      val amplitudes: List<Int>,
      val startedAt: Instant,
      val filePath: String,
    ) : AudioRecording

    data class PrerecordedWithAudioContent(
      val audioContent: AudioContent,
      val isLoading: Boolean = false,
      val hasError: Boolean = false,

      ) : AudioRecording

    data class Playback(
      val filePath: String,
      val isPlaying: Boolean,
      val isPrepared: Boolean,
      val amplitudes: List<Int>,
      val isLoading: Boolean,
      val hasError: Boolean,
      val canSubmit: Boolean,
    ) : AudioRecording
  }
}

internal sealed interface FreeTextErrorType {
  data class TooShort(val minLength: Int) : FreeTextErrorType
}

@Composable
internal fun AudioRecordingStep(
  uiState: AudioRecordingStepState,
  clock: Clock,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  startRecording: () -> Unit,
  stopRecording: () -> Unit,
  submitAudioFile: (File) -> Unit,
  submitAudioUrl: (AudioUrl) -> Unit,
  redo: () -> Unit,
  openAppSettings: () -> Unit,
  freeTextAvailable: Boolean,
  submitFreeText: () -> Unit,
  showFreeText: () -> Unit,
  showAudioRecording: () -> Unit,
  updateFreeText: (String?) -> Unit,
  onCloseFullScreenEditText: () -> Unit,
  onLaunchFullScreenEditText: () -> Unit,
  canSkip: Boolean,
  onSkip: () -> Unit,
  isCurrentStep: Boolean,
  modifier: Modifier = Modifier
  ) {
  FreeTextOverlay(
    freeTextMaxLength = 2000,
    freeTextValue = if (uiState is AudioRecordingStepState.FreeTextDescription) uiState.freeText else null,
    freeTextHint = stringResource(R.string.CLAIMS_TEXT_INPUT_POPOVER_PLACEHOLDER),
    freeTextTitle = stringResource(R.string.CLAIMS_TEXT_INPUT_PLACEHOLDER),
    freeTextOnCancelClick = {
      onCloseFullScreenEditText()
    },
    freeTextOnSaveClick = { feedback ->
      updateFreeText(feedback)
      onCloseFullScreenEditText()
    },
    shouldShowOverlay = if (uiState is AudioRecordingStepState.FreeTextDescription) uiState.showOverlay else false,
    overlaidContent = {
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
                submitAudioUrl = submitAudioUrl,
                redo = redo,
                openAppSettings = openAppSettings,
                allowFreeText = freeTextAvailable,
                launchFreeText = showFreeText,
                isCurrentStep = isCurrentStep,
                onSkip = onSkip,
                canSkip = canSkip
              )
            }

            is AudioRecordingStepState.FreeTextDescription -> {
              FreeTextInputSection(
                submitFreeText = submitFreeText,
                showAudioRecording = showAudioRecording,
                onLaunchFullScreenEditText = onLaunchFullScreenEditText,
                freeText = uiStateAnimated.freeText,
                hasError = uiStateAnimated.hasError,
                errorType = uiStateAnimated.errorType,
                canSkip = canSkip,
                onSkip = onSkip,
                isCurrentStep = isCurrentStep,
                modifier = modifier,
              )
            }
          }
        }
      }

    },
  )
}


@Composable
private fun FreeTextInputSection(
  freeText: String?,
  showAudioRecording: () -> Unit,
  onLaunchFullScreenEditText: () -> Unit,
  submitFreeText: () -> Unit,
  hasError: Boolean,
  canSkip: Boolean,
  onSkip: () -> Unit,
  isCurrentStep: Boolean,
  errorType: FreeTextErrorType?,
  modifier: Modifier = Modifier,
) {
  Column(modifier,
    horizontalAlignment = Alignment.CenterHorizontally) {
    FreeTextDisplay(
      onClick = { onLaunchFullScreenEditText() },
      freeTextValue = freeText,
      freeTextPlaceholder = stringResource(id = R.string.CLAIMS_TEXT_INPUT_PLACEHOLDER),
      supportingText = if (errorType is FreeTextErrorType.TooShort) {
        stringResource(R.string.CLAIMS_TEXT_INPUT_MIN_CHARACTERS_ERROR, errorType.minLength)
      } else {
        null
      },
      hasError = hasError,
    )
    Spacer(Modifier.height(16.dp))
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (canSkip && isCurrentStep) {
        HedvigTextButton(
          stringResource(R.string.claims_skip_button),
          onClick = onSkip,
          buttonSize = ButtonDefaults.ButtonSize.Medium,
        )
        Spacer(Modifier.width(16.dp))
      }
      if (isCurrentStep) {
        HedvigButton(
          onClick = submitFreeText,
          enabled= true,
          text = stringResource(R.string.CHAT_UPLOAD_PRESS_SEND_LABEL),
        )
      }

    }
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(R.string.CLAIMS_USE_AUDIO_RECORDING),
      onClick = showAudioRecording,
      enabled = true,
    )
  }
}

@Serializable
@JvmInline
internal value class AudioUrl(val value: String)

@Immutable
@Serializable
internal data class AudioContent(
  /**
   * The url to be used to play back the audio file
   */
  val signedUrl: AudioUrl,
  /**
   * The url that the backend expects when trying to go to the next step of the flow
   */
  val audioUrl: AudioUrl,
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun AudioRecordingSection(
  uiState: AudioRecordingStepState.AudioRecording,
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
  isCurrentStep: Boolean,
  canSkip: Boolean,
  onSkip: () -> Unit,
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
    isCurrentStep = isCurrentStep,
    canSkip = canSkip,
    onSkip = onSkip
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
        canSkip = true,
        onSkip = {},
        isCurrentStep = true,
      )
    }
  }
}
