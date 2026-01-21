package com.hedvig.feature.claim.chat.ui.audiorecording

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.RestingAudioPlayer
import com.hedvig.android.audio.player.audioplayer.rememberAudioPlayer
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.PermissionDialog
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplay
import com.hedvig.android.design.system.hedvig.icon.ArrowUp
import com.hedvig.android.design.system.hedvig.icon.Document
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Mic
import com.hedvig.android.design.system.hedvig.icon.Pause
import com.hedvig.android.design.system.hedvig.icon.Play
import com.hedvig.android.design.system.hedvig.icon.Refresh
import com.hedvig.android.design.system.hedvig.icon.Reload
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.audio.player.data.PlayableAudioSource
import com.hedvig.feature.claim.chat.data.AudioRecordingStepState
import com.hedvig.feature.claim.chat.data.FreeTextErrorType
import com.hedvig.feature.claim.chat.ui.RoundCornersPill
import com.hedvig.feature.claim.chat.ui.SkippedLabel
import hedvig.resources.AUDIO_RECORDER_LISTEN
import hedvig.resources.AUDIO_RECORDER_SEND
import hedvig.resources.AUDIO_RECORDER_START
import hedvig.resources.AUDIO_RECORDER_START_OVER
import hedvig.resources.AUDIO_RECORDER_STOP
import hedvig.resources.CLAIMS_TEXT_INPUT_MIN_CHARACTERS_ERROR
import hedvig.resources.CLAIMS_TEXT_INPUT_PLACEHOLDER
import hedvig.resources.CLAIMS_USE_AUDIO_RECORDING
import hedvig.resources.CLAIMS_USE_TEXT_INSTEAD
import hedvig.resources.CLAIM_CHAT_USE_AUDIO
import hedvig.resources.CLAIM_TRIAGING_TITLE
import hedvig.resources.PERMISSION_DIALOG_RECORD_AUDIO_MESSAGE
import hedvig.resources.Res
import hedvig.resources.SAVE_AND_CONTINUE_BUTTON_LABEL
import hedvig.resources.claims_skip_button
import kotlin.time.Clock
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AudioRecorderBubble(
  recordingState: AudioRecordingStepState,
  freeText: String?,
  clock: Clock,
  onShouldShowRequestPermissionRationale: (String) -> Boolean,
  startRecording: () -> Unit,
  stopRecording: () -> Unit,
  submitAudioFile: () -> Unit,
  redoRecording: () -> Unit,
  openAppSettings: () -> Unit,
  freeTextAvailable: Boolean,
  submitFreeText: () -> Unit,
  onSwitchToFreeText: () -> Unit,
  onSwitchToAudioRecording: () -> Unit,
  onLaunchFullScreenEditText: () -> Unit,
  canSkip: Boolean,
  onSkip: () -> Unit,
  isCurrentStep: Boolean,
  continueButtonLoading: Boolean,
  skipButtonLoading: Boolean,
  modifier: Modifier = Modifier,
) {
  AnimatedContent(
    recordingState,
    contentKey = { s ->
      when (s) {
        is AudioRecordingStepState.AudioRecording -> "audio_recording"
        is AudioRecordingStepState.FreeTextDescription -> "freetext"
      }
    },
  ) { uiStateAnimated ->
    Column(modifier) {
      when (uiStateAnimated) {

        is AudioRecordingStepState.FreeTextDescription -> {
          FreeTextInputSection(
            submitFreeText = submitFreeText,
            showAudioRecording = onSwitchToAudioRecording,
            onLaunchFullScreenEditText = onLaunchFullScreenEditText,
            freeText = freeText,
            hasError = uiStateAnimated.hasError,
            errorType = uiStateAnimated.errorType,
            isCurrentStep = isCurrentStep,
            continueButtonLoading = continueButtonLoading,
            canSubmit = uiStateAnimated.canSubmit,
          )
        }

        is AudioRecordingStepState.AudioRecording -> {
          Column(Modifier.fillMaxWidth()) {
            val state = rememberHedvigBottomSheetState<Unit>()
            AudioRecordingBottomSheet(
              audioRecordingState = uiStateAnimated,
              onDismiss = {
                state.dismiss()
              },
              clock = clock,
              shouldShowRequestPermissionRationale = onShouldShowRequestPermissionRationale,
              startRecording = startRecording,
              stopRecording = stopRecording,
              submitAudioFile = submitAudioFile,
              redo = redoRecording,
              openAppSettings = openAppSettings,
              allowFreeText = freeTextAvailable,
              launchFreeText = onSwitchToFreeText,
              isCurrentStep = isCurrentStep,
              continueButtonLoading = continueButtonLoading,
              bottomSheetState = state,
            )
            HedvigButton(
              enabled = true,
              text = stringResource(Res.string.CLAIM_CHAT_USE_AUDIO),
              onClick = {
                state.show(Unit)
              },
              modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            HedvigButton(
              enabled = true,
              buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
              text = stringResource(Res.string.CLAIMS_USE_TEXT_INSTEAD),
              onClick = onSwitchToFreeText,
              modifier = Modifier.fillMaxWidth(),
            )
          }
        }
      }

      if (canSkip && isCurrentStep) {
        Spacer(Modifier.height(8.dp))
        HedvigButton(
          stringResource(Res.string.claims_skip_button),
          onClick = onSkip,
          isLoading = skipButtonLoading,
          enabled = !skipButtonLoading,
          modifier = Modifier.fillMaxWidth(),
          buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        )
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

@Composable
private fun AudioRecordingBottomSheet(
  bottomSheetState: HedvigBottomSheetState<Unit>,
  audioRecordingState: AudioRecordingStepState.AudioRecording,
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
  continueButtonLoading: Boolean,
  modifier: Modifier = Modifier,
  onDismiss: () -> Unit,
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
  HedvigBottomSheet(bottomSheetState) {
    Column {
      HedvigText(
        stringResource(Res.string.CLAIM_TRIAGING_TITLE),
        modifier = Modifier.fillMaxWidth().semantics {
          heading()
        },
        textAlign = TextAlign.Center,
      )
      Spacer(Modifier.height(24.dp))
      if (audioRecordingState is AudioRecordingStepState.AudioRecording.Playback) {
        if (!audioRecordingState.isPrepared) {
          HedvigCircularProgressIndicator()
        } else {
          val audioPlayer = rememberAudioPlayer(PlayableAudioSource.LocalFilePath(audioRecordingState.filePath))
          HedvigAudioPlayer(
            audioPlayer = audioPlayer,
            Modifier.padding(
              horizontal = 45.dp,
              vertical = 64.dp,
            ),
          )
        }
      } else {
        RestingAudioPlayer(
          Modifier.padding(
            horizontal = 45.dp,
            vertical = 78.dp,
          ),
        )
      }
      Row(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
      ) {
        AudioButton(
          modifier = Modifier.weight(1f),
          type = AudioButtonType.StartOver(
            onStartOver = {
              //todo
            },
            isEnabled = audioRecordingState is AudioRecordingStepState.AudioRecording.Playback,
          ),
        )
        Spacer(Modifier.width(4.dp))
        AudioButton(
          modifier = Modifier.weight(1f),
          type = AudioButtonType.Control(
            onStartRecording = {
              //todo
            },
            onStopRecording = {
              //todo
            },
            onListen = {
              //todo
            },
            onPause = {
              //todo
            },
            audioRecordingState = audioRecordingState,
          ),
        )
        Spacer(Modifier.width(4.dp))
        AudioButton(
          modifier = Modifier.weight(1f),
          type = AudioButtonType.Send(
            onSend = {
              //todo
            },
            isEnabled = audioRecordingState is AudioRecordingStepState.AudioRecording.Playback,
          ),
        )
      }
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
  }
}

@Composable
private fun AudioButton(
  type: AudioButtonType,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = HedvigTheme.shapes.cornerLarge,
    modifier = modifier
      .clip(HedvigTheme.shapes.cornerLarge)
      .semantics(true) {
        role = Role.Button
      }
      .clickable(
        enabled = type.isEnabled,
        onClick = {
          when (type) {
            is AudioButtonType.Control -> when (type.audioRecordingState) {
              AudioRecordingStepState.AudioRecording.NotRecording -> type.onStartRecording
              is AudioRecordingStepState.AudioRecording.Playback -> if (type.audioRecordingState.isPrepared) {
                type.onListen
              } else if (type.audioRecordingState.isPlaying) {
                type.onPause
              } //todo: else??
              is AudioRecordingStepState.AudioRecording.Recording -> type.onStopRecording
            }

            is AudioButtonType.Send -> type.onSend
            is AudioButtonType.StartOver -> type.onStartOver
          }
        },
      ),
  ) {
    Column(
      modifier = Modifier.padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
        modifier = Modifier
          .clip(HedvigTheme.shapes.cornerXXLarge)
          .background(
          color = if (!type.isEnabled) HedvigTheme.colorScheme.surfaceSecondaryTransparent else when (type) {
            is AudioButtonType.Control -> when (type.audioRecordingState) {
              AudioRecordingStepState.AudioRecording.NotRecording -> HedvigTheme.colorScheme.signalRedElement
              is AudioRecordingStepState.AudioRecording.Playback -> HedvigTheme.colorScheme.fillPrimary
              is AudioRecordingStepState.AudioRecording.Recording -> HedvigTheme.colorScheme.signalRedElement
            }

            is AudioButtonType.Send -> HedvigTheme.colorScheme.signalBlueElement
            is AudioButtonType.StartOver -> HedvigTheme.colorScheme.fillPrimary
          },
        )
      ) {
        Icon(
          modifier = Modifier.padding(4.dp).size(24.dp),
          imageVector = when (type) {
            is AudioButtonType.Control -> when (type.audioRecordingState) {
              AudioRecordingStepState.AudioRecording.NotRecording -> HedvigIcons.Mic
              is AudioRecordingStepState.AudioRecording.Playback ->
                if (type.audioRecordingState.isPrepared) {
                  HedvigIcons.Play
                } else if (type.audioRecordingState.isPlaying) {
                  HedvigIcons.Pause
                } else HedvigIcons.Play //todo: check here
              is AudioRecordingStepState.AudioRecording.Recording -> HedvigIcons.Pause
            }

            is AudioButtonType.Send -> HedvigIcons.ArrowUp
            is AudioButtonType.StartOver -> HedvigIcons.Reload
          },
          contentDescription = EmptyContentDescription,
          tint = if (!type.isEnabled) HedvigTheme.colorScheme.fillTertiary else HedvigTheme.colorScheme.fillNegative,
        )
      }
      Spacer(Modifier.height(8.dp))
      HedvigText(
        text = when (type) {
          is AudioButtonType.Control -> when (type.audioRecordingState) {
            AudioRecordingStepState.AudioRecording.NotRecording -> stringResource(Res.string.AUDIO_RECORDER_START)
            is AudioRecordingStepState.AudioRecording.Playback -> stringResource(Res.string.AUDIO_RECORDER_LISTEN)
            is AudioRecordingStepState.AudioRecording.Recording -> stringResource(Res.string.AUDIO_RECORDER_STOP)
          }

          is AudioButtonType.Send -> stringResource(Res.string.AUDIO_RECORDER_SEND)
          is AudioButtonType.StartOver -> stringResource(Res.string.AUDIO_RECORDER_START_OVER)
        },
        fontStyle = HedvigTheme.typography.label.fontStyle,
        color = if (type.isEnabled) HedvigTheme.colorScheme.textPrimary else HedvigTheme.colorScheme.textTertiary,
      )
    }
  }
}

private sealed interface AudioButtonType {
  val isEnabled: Boolean

  class StartOver(
    val onStartOver: () -> Unit,
    override val isEnabled: Boolean,
  ) : AudioButtonType

  class Control(
    val onStartRecording: () -> Unit,
    val onStopRecording: () -> Unit,
    val onListen: () -> Unit,
    val onPause: () -> Unit,
    val audioRecordingState: AudioRecordingStepState.AudioRecording,
  ) : AudioButtonType {
    override val isEnabled: Boolean
      get() = true
  }

  class Send(
    val onSend: () -> Unit,
    override val isEnabled: Boolean,
  ) : AudioButtonType
}

@Composable
private fun FreeTextInputSection(
  freeText: String?,
  showAudioRecording: () -> Unit,
  onLaunchFullScreenEditText: () -> Unit,
  submitFreeText: () -> Unit,
  hasError: Boolean,
  isCurrentStep: Boolean,
  continueButtonLoading: Boolean,
  errorType: FreeTextErrorType?,
  canSubmit: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier,
  ) {
    if (isCurrentStep) {
      FreeTextDisplay(
        onClick = { onLaunchFullScreenEditText() },
        freeTextValue = freeText,
        freeTextPlaceholder = stringResource(Res.string.CLAIMS_TEXT_INPUT_PLACEHOLDER),
        supportingText = when (errorType) {
          is FreeTextErrorType.TooShort ->
            stringResource(
              Res.string.CLAIMS_TEXT_INPUT_MIN_CHARACTERS_ERROR,
              errorType.minLength,
            )

          else -> null
        },
        hasError = hasError,
      )
      Spacer(Modifier.height(16.dp))
      HedvigButton(
        onClick = submitFreeText,
        enabled = canSubmit,
        isLoading = continueButtonLoading,
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(Res.string.SAVE_AND_CONTINUE_BUTTON_LABEL),
      )
      Spacer(Modifier.height(8.dp))
      HedvigButton(
        text = stringResource(Res.string.CLAIMS_USE_AUDIO_RECORDING),
        onClick = showAudioRecording,
        modifier = Modifier.fillMaxWidth(),
        enabled = true,
        buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      )
    } else {
      if (freeText != null) {
        Row(
          Modifier.fillMaxWidth().padding(start = 48.dp),
          horizontalArrangement = Arrangement.End,
        ) {
          RoundCornersPill(
            onClick = null,
          ) {
            HedvigText(freeText, textAlign = TextAlign.End)
          }
        }

      } else {
        SkippedLabel()
      }
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
  continueButtonLoading: Boolean,
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
    continueButtonLoading = continueButtonLoading,
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
        continueButtonLoading = false,
        canSubmit = true,
      )
    }
  }
}

// Platform-specific permission constant
internal expect val RECORD_AUDIO_PERMISSION: String
