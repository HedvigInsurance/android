package com.hedvig.android.feature.claim.chat.ui.step.audiorecording

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.audioplayer.rememberAudioPlayer
import com.hedvig.android.audio.player.data.AudioPlayer
import com.hedvig.android.audio.player.data.AudioPlayerState
import com.hedvig.android.audio.player.data.PlayableAudioSource
import com.hedvig.android.audio.player.data.ProgressPercentage
import com.hedvig.android.audio.player.data.SignedAudioUrl
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.compose.ui.withoutPlacement
import com.hedvig.android.core.uidata.DecimalFormatter
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.IconButton
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.PermissionDialog
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplay
import com.hedvig.android.design.system.hedvig.icon.ArrowUp
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Mic
import com.hedvig.android.design.system.hedvig.icon.Pause
import com.hedvig.android.design.system.hedvig.icon.PenEdit
import com.hedvig.android.design.system.hedvig.icon.Play
import com.hedvig.android.design.system.hedvig.icon.Reload
import com.hedvig.android.design.system.hedvig.icon.Stop
import com.hedvig.android.feature.claim.chat.ClaimChatEvent
import com.hedvig.android.feature.claim.chat.FreeTextRestrictions
import com.hedvig.android.feature.claim.chat.data.AudioPath
import com.hedvig.android.feature.claim.chat.data.AudioRecordingStepState
import com.hedvig.android.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.android.feature.claim.chat.data.FreeTextErrorType
import com.hedvig.android.feature.claim.chat.data.StepContent
import com.hedvig.android.feature.claim.chat.ui.common.EditButton
import com.hedvig.android.feature.claim.chat.ui.common.RoundCornersPill
import com.hedvig.android.feature.claim.chat.ui.common.SkippedLabel
import com.hedvig.android.feature.claim.chat.ui.sentAnswersStartPadding
import hedvig.resources.AUDIO_RECORDER_LISTEN
import hedvig.resources.AUDIO_RECORDER_SEND
import hedvig.resources.AUDIO_RECORDER_START
import hedvig.resources.AUDIO_RECORDER_START_OVER
import hedvig.resources.AUDIO_RECORDER_STOP
import hedvig.resources.CLAIMS_TEXT_INPUT_MIN_CHARACTERS_ERROR
import hedvig.resources.CLAIMS_TEXT_INPUT_PLACEHOLDER
import hedvig.resources.CLAIMS_USE_AUDIO_RECORDING
import hedvig.resources.CLAIM_CHAT_USE_AUDIO
import hedvig.resources.CLAIM_CHAT_USE_TEXT_INPUT
import hedvig.resources.CLAIM_TRIAGING_TITLE
import hedvig.resources.PERMISSION_DIALOG_RECORD_AUDIO_MESSAGE
import hedvig.resources.Res
import hedvig.resources.SAVE_AND_CONTINUE_BUTTON_LABEL
import hedvig.resources.TALKBACK_CLAIM_CHAT_YOUR_ANSWER
import hedvig.resources.TALKBACK_PLAYBACK_BUTTON_STATE
import hedvig.resources.TALKBACK_RECORDING_DURATION
import hedvig.resources.TALKBACK_RECORDING_NOW
import hedvig.resources.claims_record
import hedvig.resources.claims_skip_button
import hedvig.resources.claims_write
import hedvig.resources.general_cancel_button
import hedvig.resources.general_close_button
import hedvig.resources.something_went_wrong
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AudioRecordingStep(
  item: ClaimIntentStep,
  stepContent: StepContent.AudioRecording,
  onShowFreeText: () -> Unit,
  onSwitchToAudioRecording: () -> Unit,
  onLaunchFullScreenEditText: (restrictions: FreeTextRestrictions) -> Unit,
  submitFreeText: () -> Unit,
  submitAudioFile: () -> Unit,
  stopRecording: () -> Unit,
  redoRecording: () -> Unit,
  onSkip: () -> Unit,
  isCurrentStep: Boolean,
  continueButtonLoading: Boolean,
  skipButtonLoading: Boolean,
  clock: Clock,
  onShouldShowRequestPermissionRationale: (String) -> Boolean,
  openAppSettings: () -> Unit,
  startRecording: () -> Unit,
  onEvent: (ClaimChatEvent) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
    AudioRecorderBubble(
      recordingState = stepContent.recordingState,
      clock = clock,
      onShouldShowRequestPermissionRationale = onShouldShowRequestPermissionRationale,
      startRecording = startRecording,
      stopRecording = stopRecording,
      submitAudioFile = submitAudioFile,
      redoRecording = redoRecording,
      openAppSettings = openAppSettings,
      freeTextAvailable = true,
      submitFreeText = submitFreeText,
      onSwitchToFreeText = onShowFreeText,
      onSwitchToAudioRecording = onSwitchToAudioRecording,
      onLaunchFullScreenEditText = {
        onLaunchFullScreenEditText(
          FreeTextRestrictions(
            stepContent.freeTextMinLength,
            stepContent.freeTextMaxLength,
          ),
        )
      },
      onSaveFreeText = { text -> onEvent(ClaimChatEvent.UpdateFreeText(text)) },
      onCancelSubmission = { onEvent(ClaimChatEvent.AudioRecording.CancelTextSubmission) },
      freeTextMinLength = stepContent.freeTextMinLength,
      freeTextMaxLength = stepContent.freeTextMaxLength,
      canSkip = stepContent.isSkippable,
      onSkip = onSkip,
      isCurrentStep = isCurrentStep,
      continueButtonLoading = continueButtonLoading,
      skipButtonLoading = skipButtonLoading,
    )
    EditButton(
      canBeChanged = item.isRegrettable && !isCurrentStep,
      onRegret = {
        onEvent(ClaimChatEvent.ShowConfirmEditDialog(item.id))
      },
    )
  }
}

@Composable
internal fun AudioRecorderBubble(
  recordingState: AudioRecordingStepState,
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
  onSaveFreeText: (String) -> Unit,
  onCancelSubmission: () -> Unit,
  freeTextMinLength: Int,
  freeTextMaxLength: Int,
  canSkip: Boolean,
  onSkip: () -> Unit,
  isCurrentStep: Boolean,
  continueButtonLoading: Boolean,
  skipButtonLoading: Boolean,
  modifier: Modifier = Modifier,
) {
  val isSubmitting = continueButtonLoading || skipButtonLoading
  val focusManager = LocalFocusManager.current
  // The voice card is open either because the user asked for it or because a recording is already in flight.
  var voiceCardRequested by remember(isCurrentStep) { mutableStateOf(false) }
  val hasRecording = recordingState is AudioRecordingStepState.AudioRecording &&
    recordingState !is AudioRecordingStepState.AudioRecording.NotRecording

  Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    if (!isCurrentStep) {
      when {
        recordingState is AudioRecordingStepState.FreeTextDescription && recordingState.freeText != null -> {
          val description = stringResource(Res.string.TALKBACK_CLAIM_CHAT_YOUR_ANSWER) + recordingState.freeText
          RoundCornersPill(
            modifier = Modifier.fillMaxWidth()
              .padding(start = 48.dp)
              .wrapContentWidth(Alignment.End)
              .clearAndSetSemantics { contentDescription = description },
          ) {
            HedvigText(recordingState.freeText, textAlign = TextAlign.End)
          }
        }

        recordingState is AudioRecordingStepState.AudioRecording.Playback -> {
          val audioPlayer = when (recordingState.audioPath) {
            is AudioPath.FilePath -> rememberAudioPlayer(
              PlayableAudioSource.LocalFilePath(recordingState.audioPath.filePath),
            )

            is AudioPath.RemoteUrl -> rememberAudioPlayer(
              PlayableAudioSource.RemoteUrl(
                SignedAudioUrl.fromSignedAudioUrlString(recordingState.audioPath.remoteUrl),
              ),
            )
          }
          HedvigAudioPlayer(audioPlayer = audioPlayer, Modifier.padding(start = sentAnswersStartPadding))
        }

        else -> {
          SkippedLabel()
        }
      }
    } else {
      AnimatedContent(
        targetState = when {
          recordingState is AudioRecordingStepState.FreeTextDescription -> InputMode.Text
          voiceCardRequested || hasRecording -> InputMode.Voice
          else -> InputMode.Resting
        },
        modifier = Modifier.fillMaxWidth(),
      ) { mode ->
        when (mode) {
          InputMode.Text -> {
            val freeText = recordingState as? AudioRecordingStepState.FreeTextDescription
            InlineTextAnswerCard(
              initialText = freeText?.freeText.orEmpty(),
              minLength = freeTextMinLength,
              maxLength = freeTextMaxLength,
              errorType = freeText?.errorType,
              hasError = freeText?.hasError == true,
              isSubmitting = isSubmitting,
              onCancel = {
                focusManager.clearFocus()
                // Calls off an answer still in flight before leaving, so Avbryt does what it says rather
                // than closing over a submission that lands anyway.
                onCancelSubmission()
                onSwitchToAudioRecording()
              },
              onSave = { text ->
                focusManager.clearFocus()
                onSaveFreeText(text)
                submitFreeText()
              },
            )
          }

          InputMode.Voice -> {
            InlineVoiceAnswerCard(
              audioRecordingState = recordingState as? AudioRecordingStepState.AudioRecording
                ?: AudioRecordingStepState.AudioRecording.NotRecording,
              clock = clock,
              shouldShowRequestPermissionRationale = onShouldShowRequestPermissionRationale,
              startRecording = startRecording,
              stopRecording = stopRecording,
              submitAudioFile = submitAudioFile,
              redo = redoRecording,
              openAppSettings = openAppSettings,
              isSubmitting = isSubmitting,
              onClose = {
                stopRecording()
                voiceCardRequested = false
                onSwitchToAudioRecording()
              },
            )
          }

          InputMode.Resting -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                if (freeTextAvailable) {
                  HedvigButton(
                    onClick = {
                      focusManager.clearFocus()
                      onSwitchToFreeText()
                    },
                    enabled = true,
                    buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
                    buttonSize = ButtonDefaults.ButtonSize.Large,
                    modifier = Modifier.weight(1f),
                  ) {
                    Icon(HedvigIcons.PenEdit, null, Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    HedvigText(stringResource(Res.string.claims_write))
                  }
                }
                HedvigButton(
                  onClick = {
                    focusManager.clearFocus()
                    voiceCardRequested = true
                  },
                  enabled = true,
                  buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
                  buttonSize = ButtonDefaults.ButtonSize.Large,
                  modifier = Modifier.weight(1f),
                ) {
                  Icon(HedvigIcons.Mic, null, Modifier.size(24.dp))
                  Spacer(Modifier.width(8.dp))
                  HedvigText(stringResource(Res.string.claims_record))
                }
              }
              if (canSkip) {
                HedvigButton(
                  stringResource(Res.string.claims_skip_button),
                  onClick = onSkip,
                  isLoading = skipButtonLoading,
                  enabled = !isSubmitting,
                  modifier = Modifier.fillMaxWidth(),
                  buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
                )
              }
            }
          }
        }
      }
    }
  }
}

private enum class InputMode { Resting, Text, Voice }

@Composable
private fun InlineVoiceAnswerCard(
  onClose: () -> Unit,
  audioRecordingState: AudioRecordingStepState.AudioRecording,
  clock: Clock,
  shouldShowRequestPermissionRationale: (String) -> Boolean,
  startRecording: () -> Unit,
  stopRecording: () -> Unit,
  submitAudioFile: () -> Unit,
  redo: () -> Unit,
  openAppSettings: () -> Unit,
  isSubmitting: Boolean,
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

  val audioPlayer = (audioRecordingState as? AudioRecordingStepState.AudioRecording.Playback)?.let {
    if (it.audioPath is AudioPath.FilePath) {
      rememberAudioPlayer(
        PlayableAudioSource.LocalFilePath(it.audioPath.filePath),
      )
    } else {
      null
    }
  }

  LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
    stopRecording()
  }
  val isShortWindow = with(LocalDensity.current) {
    LocalWindowInfo.current.containerSize.height.toDp()
  } < SHORT_WINDOW_MAX_HEIGHT
  // An inline card rather than a sheet: the design keeps the question fully readable above it, with no scrim.
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = HedvigTheme.shapes.cornerXLarge,
    color = HedvigTheme.colorScheme.surfacePrimary,
  ) {
    Box(Modifier.padding(16.dp)) {
      IconButton(
        onClick = onClose,
        modifier = Modifier.align(Alignment.TopEnd).size(24.dp),
      ) {
        Icon(HedvigIcons.Close, stringResource(Res.string.general_close_button), Modifier.size(24.dp))
      }
      AudioRecordingSheetContent(
        clock = clock,
        submitAudioFile = submitAudioFile,
        redo = redo,
        isSubmitting = isSubmitting,
        audioPlayer = audioPlayer,
        audioRecordingState = audioRecordingState,
        stopRecording = stopRecording,
        recordAudioPermissionState = recordAudioPermissionState,
        startRecording = startRecording,
        isShortWindow = isShortWindow,
        // The close button is drawn over the content, so the trailing controls have to end short of it.
        modifier = Modifier.padding(end = if (isShortWindow) CLOSE_BUTTON_CLEARANCE else 0.dp),
      )
    }
  }
}

/**
 * Text answer for a claim chat step, sitting directly above the keyboard.
 *
 * The field itself is the input, so focusing it raises the keyboard and the card rides above it. There is no
 * overlay and no scrim, which keeps the question readable while answering.
 */
@Composable
private fun InlineTextAnswerCard(
  initialText: String,
  minLength: Int,
  maxLength: Int,
  errorType: FreeTextErrorType?,
  hasError: Boolean,
  isSubmitting: Boolean,
  onCancel: () -> Unit,
  onSave: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var text by remember { mutableStateOf(initialText) }
  // The card holds its own text, so the step's `canSubmit` only catches up on save. The length rule has to be
  // applied here or nothing applies it before the answer is already sent.
  val isLongEnough = text.trim().length >= minLength
  val focusRequester = remember { FocusRequester() }
  LaunchedEffect(Unit) {
    runCatching { focusRequester.requestFocus() }
  }
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = HedvigTheme.shapes.cornerXLarge,
    color = HedvigTheme.colorScheme.surfacePrimary,
  ) {
    Column(Modifier.padding(16.dp)) {
      HedvigText(
        stringResource(Res.string.CLAIM_TRIAGING_TITLE),
        style = HedvigTheme.typography.label,
        color = HedvigTheme.colorScheme.textSecondary,
      )
      HedvigTextField(
        text = text,
        onValueChange = { if (it.length <= maxLength) text = it },
        labelText = "",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Small,
        singleLine = false,
        // The field starts at one line and grows with the answer, then scrolls inside itself rather than
        // pushing the card any further up the conversation.
        maxLines = TEXT_ANSWER_MAX_LINES,
        readOnly = isSubmitting,
        // The card is the surface here, exactly as the Figma draws it: one card with the answer written straight
        // onto it. The field's own background would be a second surface the design does not have, and its
        // focus shift would arrive as a lighter box inside the card.
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
      )
      // Says why send is out of reach, rather than leaving a disabled button to explain itself. Only once the
      // member has started writing: on an empty field the placeholder is the instruction.
      if ((hasError && errorType is FreeTextErrorType.TooShort) || (text.isNotBlank() && !isLongEnough)) {
        HedvigText(
          stringResource(Res.string.CLAIMS_TEXT_INPUT_MIN_CHARACTERS_ERROR, minLength),
          style = HedvigTheme.typography.label,
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
      Spacer(Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
      ) {
        // Stays tappable while the answer is in flight, which is the only way out of a submission that
        // is taking too long.
        HedvigButton(
          text = stringResource(Res.string.general_cancel_button),
          onClick = onCancel,
          enabled = true,
          buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
          buttonSize = ButtonDefaults.ButtonSize.Medium,
        )
        HedvigButton(
          text = stringResource(Res.string.AUDIO_RECORDER_SEND),
          onClick = { onSave(text) },
          enabled = isLongEnough && !isSubmitting,
          isLoading = isSubmitting,
          buttonSize = ButtonDefaults.ButtonSize.Medium,
        )
      }
    }
  }
}

@Composable
private fun AudioRecordingSheetContent(
  clock: Clock,
  submitAudioFile: () -> Unit,
  redo: () -> Unit,
  isSubmitting: Boolean,
  audioPlayer: AudioPlayer?,
  audioRecordingState: AudioRecordingStepState.AudioRecording,
  stopRecording: () -> Unit,
  startRecording: () -> Unit,
  recordAudioPermissionState: PermissionState,
  isShortWindow: Boolean,
  modifier: Modifier = Modifier,
) {
  if (isShortWindow) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
      AudioWaveBand(
        audioRecordingState = audioRecordingState,
        audioPlayer = audioPlayer,
        modifier = Modifier.weight(1f),
        horizontalInset = WAVE_BAND_ROW_INSET,
      )
      Column(
        // Sized to the controls, which are the widest thing in it. The heading and the clock centre
        // themselves inside that rather than claiming width the waveform is sharing.
        modifier = Modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        AudioRecordingHeading()
        DynamicClock(audioRecordingState, clock, audioPlayer, Modifier.fillMaxWidth())
        AudioRecordingControls(
          submitAudioFile = submitAudioFile,
          redo = redo,
          isSubmitting = isSubmitting,
          audioPlayer = audioPlayer,
          audioRecordingState = audioRecordingState,
          stopRecording = stopRecording,
          startRecording = startRecording,
          recordAudioPermissionState = recordAudioPermissionState,
          fillWidth = false,
        )
      }
    }
  } else {
    Column(modifier) {
      // Kept clear of the close button drawn over the top corner, which a long heading runs under at
      // large font scales.
      AudioRecordingHeading(Modifier.fillMaxWidth().padding(horizontal = CLOSE_BUTTON_CLEARANCE))
      DynamicClock(audioRecordingState, clock, audioPlayer, Modifier.fillMaxWidth())
      AudioWaveBand(
        audioRecordingState = audioRecordingState,
        audioPlayer = audioPlayer,
        modifier = Modifier.fillMaxWidth(),
      )
      AudioRecordingControls(
        submitAudioFile = submitAudioFile,
        redo = redo,
        isSubmitting = isSubmitting,
        audioPlayer = audioPlayer,
        audioRecordingState = audioRecordingState,
        stopRecording = stopRecording,
        startRecording = startRecording,
        recordAudioPermissionState = recordAudioPermissionState,
        fillWidth = true,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun AudioRecordingHeading(modifier: Modifier = Modifier) {
  HedvigText(
    stringResource(Res.string.CLAIM_TRIAGING_TITLE),
    modifier = modifier.semantics {
      heading()
    },
    textAlign = TextAlign.Center,
  )
}

@Composable
private fun AudioWaveBand(
  audioRecordingState: AudioRecordingStepState.AudioRecording,
  audioPlayer: AudioPlayer?,
  modifier: Modifier = Modifier,
  horizontalInset: Dp = WAVE_BAND_HORIZONTAL_INSET,
) {
  BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
    // Too few bars fit to read as a waveform at all, so it leaves rather than being drawn as a stub.
    if (maxWidth < MINIMUM_WAVE_BAND_WIDTH) return@BoxWithConstraints
    AudioWaveBandContent(audioRecordingState, audioPlayer, horizontalInset)
  }
}

@Composable
private fun AudioWaveBandContent(
  audioRecordingState: AudioRecordingStepState.AudioRecording,
  audioPlayer: AudioPlayer?,
  horizontalInset: Dp,
) {
  AnimatedContent(
    // widthIn before fillMaxWidth: the cap lowers the width offered, then the band fills whatever is left.
    modifier = Modifier.widthIn(max = MAXIMUM_WAVE_BAND_WIDTH).fillMaxWidth(),
    targetState = audioRecordingState,
    transitionSpec = {
      EnterTransition.None.togetherWith(ExitTransition.None)
    },
    contentKey = { state ->
      when (state) {
        is AudioRecordingStepState.AudioRecording.Playback -> {
          if (state.isPrepared) "playback" else "loading"
        }

        is AudioRecordingStepState.AudioRecording.Recording -> {
          "recording"
        }

        else -> {
          "resting"
        }
      }
    },
  ) { target ->
    Box(
      modifier = Modifier
        .padding(horizontal = horizontalInset, vertical = WAVE_BAND_VERTICAL_INSET)
        .heightIn(min = WAVE_MAX_HEIGHT),
      contentAlignment = Alignment.Center,
      propagateMinConstraints = true,
    ) {
      when (target) {
        is AudioRecordingStepState.AudioRecording.Playback if !target.isPrepared && !target.hasError -> {
          HedvigCircularProgressIndicator(Modifier.wrapContentSize())
        }

        is AudioRecordingStepState.AudioRecording.Playback if target.hasError -> {
          EmptyState(
            text = stringResource(Res.string.something_went_wrong),
            modifier = Modifier,
            iconStyle = EmptyStateDefaults.EmptyStateIconStyle.ERROR,
            description = null,
          )
        }

        is AudioRecordingStepState.AudioRecording.Playback -> {
          val audioPlayerState by audioPlayer?.audioPlayerState?.collectAsStateWithLifecycle()
            ?: remember { mutableStateOf(null) }
          if (audioPlayerState is AudioPlayerState.Ready) {
            AudioWaves(
              isRecording = false,
              progressPercentage = (audioPlayerState as AudioPlayerState.Ready).progressPercentage,
            )
          }
        }

        is AudioRecordingStepState.AudioRecording.Recording -> {
          AudioWaves(
            isRecording = true,
            progressPercentage = null,
            amplitudes = target.amplitudes,
          )
        }

        else -> {
          RestingAudioPlayer()
        }
      }
    }
  }
}

@Composable
private fun AudioRecordingControls(
  submitAudioFile: () -> Unit,
  redo: () -> Unit,
  isSubmitting: Boolean,
  audioPlayer: AudioPlayer?,
  audioRecordingState: AudioRecordingStepState.AudioRecording,
  stopRecording: () -> Unit,
  startRecording: () -> Unit,
  recordAudioPermissionState: PermissionState,
  fillWidth: Boolean,
  modifier: Modifier = Modifier,
) {
  EqualWidthRow(
    horizontalSpacing = 4.dp,
    fillWidth = fillWidth,
    modifier = modifier,
  ) {
    StartOverButton(
      onStartOver = redo,
      isEnabled = audioRecordingState is AudioRecordingStepState.AudioRecording.Playback && !isSubmitting,
    )
    ControlButton(
      audioPlayer = audioPlayer,
      onStartRecording = {
        when (recordAudioPermissionState.status) {
          PermissionStatus.Granted -> startRecording()
          is PermissionStatus.Denied -> recordAudioPermissionState.launchPermissionRequest()
        }
      },
      onStopRecording = stopRecording,
      audioRecordingState = audioRecordingState,
      isEnabled = !isSubmitting,
    )
    SendButton(
      onSend = submitAudioFile,
      isEnabled = audioRecordingState is AudioRecordingStepState.AudioRecording.Playback && !isSubmitting,
    )
  }
}

/**
 * Lays children out in a row, every one of them as wide as the widest.
 *
 * The labels under the control icons differ in length, and the difference grows with the locale and the
 * user's font scale, so letting each button take its own width leaves the group visibly lopsided and shifts
 * the buttons sideways as the label changes between states.
 *
 * With [fillWidth] the children share the full width between them; without it the row wraps the widest
 * child, which is what a row laid out along a short window's free width needs.
 */
@Composable
private fun EqualWidthRow(
  horizontalSpacing: Dp,
  fillWidth: Boolean,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  val measurePolicy = remember(horizontalSpacing, fillWidth) {
    object : MeasurePolicy {
      override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
        if (measurables.isEmpty()) return layout(0, 0) {}
        val spacing = horizontalSpacing.roundToPx()
        val totalSpacing = spacing * (measurables.size - 1)
        val available = (constraints.maxWidth - totalSpacing).coerceAtLeast(0)
        val share = available / measurables.size
        val childWidth = if (fillWidth) {
          share
        } else {
          minOf(share, measurables.maxOf { it.maxIntrinsicWidth(constraints.maxHeight) })
        }
        val placeables = measurables.map {
          it.measure(constraints.copy(minWidth = childWidth, maxWidth = childWidth, minHeight = 0))
        }
        val height = placeables.maxOf { it.height }
        return layout(placeables.sumOf { it.width } + totalSpacing, height) {
          var x = 0
          for (placeable in placeables) {
            placeable.place(x, (height - placeable.height) / 2)
            x += placeable.width + spacing
          }
        }
      }

      // The default would add up the children's own widths, which is the lopsided total this row exists to
      // avoid, and a parent sizing itself to that would then squeeze the widest child until its label wraps.
      override fun IntrinsicMeasureScope.maxIntrinsicWidth(measurables: List<IntrinsicMeasurable>, height: Int): Int {
        if (measurables.isEmpty()) return 0
        val widest = measurables.maxOf { it.maxIntrinsicWidth(height) }
        return widest * measurables.size + horizontalSpacing.roundToPx() * (measurables.size - 1)
      }
    }
  }
  Layout(content, modifier, measurePolicy)
}

@Composable
private fun DynamicClock(
  audioRecordingState: AudioRecordingStepState.AudioRecording,
  clock: Clock,
  audioPlayer: AudioPlayer?,
  modifier: Modifier = Modifier,
) {
  data class TimerState(
    val minutes: String,
    val seconds: String,
  ) {
    override fun toString(): String {
      return "$minutes:$seconds"
    }
  }

  val startedRecordingAt by remember {
    mutableStateOf<Instant?>(null)
  }.apply {
    if (audioRecordingState is AudioRecordingStepState.AudioRecording.Recording) {
      value = audioRecordingState.startedAt
    }
  }

  val audioPlayerState by audioPlayer?.audioPlayerState?.collectAsStateWithLifecycle()
    ?: remember { mutableStateOf<AudioPlayerState?>(null) }

  val twoDigitsFormat = remember { DecimalFormatter("00") }

  val timerState = when (audioRecordingState) {
    is AudioRecordingStepState.AudioRecording.Recording -> {
      val diff = clock.now() - (startedRecordingAt ?: clock.now())
      TimerState(
        twoDigitsFormat.format(diff.inWholeMinutes),
        twoDigitsFormat.format(diff.inWholeSeconds % 60),
      )
    }

    is AudioRecordingStepState.AudioRecording.Playback -> {
      val ready = audioPlayerState as? AudioPlayerState.Ready
      if (ready != null) {
        val durationSeconds = ready.durationMillis / 1000
        TimerState(
          twoDigitsFormat.format(durationSeconds / 60),
          twoDigitsFormat.format(durationSeconds % 60),
        )
      } else {
        null
      }
    }

    else -> {
      null
    }
  }

  // Before anything is recorded the clock reads 00:00 rather than going blank, so it stays paired with the
  // heading above it instead of the heading appearing to sit on its own.
  val zeroed = timerState == null
  val shownState = timerState ?: TimerState(twoDigitsFormat.format(0), twoDigitsFormat.format(0))

  val durationDescription = if (zeroed) null else stringResource(Res.string.TALKBACK_RECORDING_DURATION, shownState)

  Box(
    modifier.clearAndSetSemantics {
      if (durationDescription != null) {
        contentDescription = durationDescription
      }
    }.wrapContentWidth(),
  ) {
    HedvigText(
      text = ":",
      color = HedvigTheme.colorScheme.textSecondary,
    )
    HedvigText(
      text = shownState.minutes,
      modifier = Modifier.requiredWidth(0.dp).align(Alignment.CenterStart).wrapContentWidth(Alignment.End, true),
      color = HedvigTheme.colorScheme.textSecondary,
    )
    HedvigText(
      text = shownState.seconds,
      modifier = Modifier.requiredWidth(0.dp).align(Alignment.CenterEnd).wrapContentWidth(Alignment.Start, true),
      color = HedvigTheme.colorScheme.textSecondary,
    )
  }
}

@Composable
private fun StartOverButton(onStartOver: () -> Unit, isEnabled: Boolean, modifier: Modifier = Modifier) {
  Surface(
    shape = HedvigTheme.shapes.cornerLarge,
    modifier = modifier.clip(HedvigTheme.shapes.cornerLarge).semantics(true) {
      role = Role.Button
    }.clickable(
      enabled = isEnabled,
      onClick = onStartOver,
    ),
  ) {
    Column(
      modifier = Modifier.padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
        modifier = Modifier.clip(HedvigTheme.shapes.cornerXXLarge).background(
          color = if (!isEnabled) {
            HedvigTheme.colorScheme.surfaceSecondaryTransparent
          } else {
            HedvigTheme.colorScheme.surfaceSecondaryTransparent
          },
        ),
      ) {
        Icon(
          modifier = Modifier.padding(4.dp).size(24.dp),
          imageVector = HedvigIcons.Reload,
          contentDescription = EmptyContentDescription,
          tint = if (!isEnabled) {
            HedvigTheme.colorScheme.fillTertiary
          } else {
            HedvigTheme.colorScheme.fillPrimary
          },
        )
      }
      Spacer(Modifier.height(4.dp))
      HedvigText(
        text = stringResource(Res.string.AUDIO_RECORDER_START_OVER),
        fontSize = HedvigTheme.typography.label.fontSize,
        fontStyle = HedvigTheme.typography.label.fontStyle,
        color = if (isEnabled) HedvigTheme.colorScheme.textPrimary else HedvigTheme.colorScheme.textTertiary,
      )
    }
  }
}

@Composable
private fun ControlButton(
  audioPlayer: AudioPlayer?,
  onStartRecording: () -> Unit,
  onStopRecording: () -> Unit,
  audioRecordingState: AudioRecordingStepState.AudioRecording,
  isEnabled: Boolean,
  modifier: Modifier = Modifier,
) {
  val audioPlayerState by audioPlayer?.audioPlayerState?.collectAsStateWithLifecycle()
    ?: remember { mutableStateOf<AudioPlayerState?>(null) }

  val onClickLabel = when (audioRecordingState) {
    AudioRecordingStepState.AudioRecording.NotRecording -> stringResource(Res.string.AUDIO_RECORDER_START)
    is AudioRecordingStepState.AudioRecording.Playback -> stringResource(Res.string.AUDIO_RECORDER_LISTEN)
    is AudioRecordingStepState.AudioRecording.Recording -> stringResource(Res.string.AUDIO_RECORDER_STOP)
  }
  var countDownText by remember { mutableStateOf("3") }
  var startRecordingCountdown by remember { mutableStateOf(false) }
  val recordingStateDescription = when (audioRecordingState) {
    is AudioRecordingStepState.AudioRecording.Recording -> stringResource(Res.string.TALKBACK_RECORDING_NOW)
    is AudioRecordingStepState.AudioRecording.Playback -> stringResource(Res.string.TALKBACK_PLAYBACK_BUTTON_STATE)
    is AudioRecordingStepState.AudioRecording.NotRecording -> if (startRecordingCountdown) countDownText else ""
  }

  val scale = remember { Animatable(1f) }
  val hapticFeedback = LocalHapticFeedback.current
  val lifecycleOwner = LocalLifecycleOwner.current
  LaunchedEffect(startRecordingCountdown, lifecycleOwner) {
    if (startRecordingCountdown) {
      val maxScale = 1.3f
      hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
      scale.animateTo(maxScale, animationSpec = tween(durationMillis = 200))
      scale.animateTo(1f, animationSpec = tween(durationMillis = 300))
      delay(500)

      countDownText = "2"
      hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
      scale.animateTo(maxScale, animationSpec = tween(durationMillis = 200))
      scale.animateTo(1f, animationSpec = tween(durationMillis = 300))
      delay(500)

      countDownText = "1"
      hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
      scale.animateTo(maxScale, animationSpec = tween(durationMillis = 200))
      scale.animateTo(1f, animationSpec = tween(durationMillis = 300))
      delay(500)

      if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        onStartRecording()
      }
      startRecordingCountdown = false
    }
    countDownText = "3"
    scale.snapTo(1f)
  }
  // custom description for button's "play" state includes its role and click label. Needed
  // because when the recording is stopped but the focus is still on the same button,
  // only the state updates, the role and label are not announced.
  val hideFromA11y = audioRecordingState is AudioRecordingStepState.AudioRecording.Playback
  Surface(
    shape = HedvigTheme.shapes.cornerLarge,
    modifier = modifier.clip(HedvigTheme.shapes.cornerLarge).semantics {
      stateDescription = recordingStateDescription
    }.clickable(
      enabled = isEnabled,
      onClickLabel = if (hideFromA11y) null else onClickLabel,
      role = if (hideFromA11y) null else Role.Button,
      onClick = {
        when (audioRecordingState) {
          AudioRecordingStepState.AudioRecording.NotRecording -> {
            startRecordingCountdown = true
          }

          is AudioRecordingStepState.AudioRecording.Playback -> {
            val ready = audioPlayerState as? AudioPlayerState.Ready
            if (ready?.readyState is AudioPlayerState.Ready.ReadyState.Playing) {
              audioPlayer?.pausePlayer()
            } else {
              audioPlayer?.startPlayer()
            }
          }

          is AudioRecordingStepState.AudioRecording.Recording -> {
            onStopRecording()
          }
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
          .graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
          }
          .clip(HedvigTheme.shapes.cornerXXLarge)
          .background(
            color = if (!isEnabled) {
              HedvigTheme.colorScheme.surfaceSecondaryTransparent
            } else {
              when (audioRecordingState) {
                AudioRecordingStepState.AudioRecording.NotRecording -> HedvigTheme.colorScheme.signalRedElement
                is AudioRecordingStepState.AudioRecording.Playback -> HedvigTheme.colorScheme.fillPrimary
                is AudioRecordingStepState.AudioRecording.Recording -> HedvigTheme.colorScheme.signalRedElement
              }
            },
          ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          modifier = Modifier
            .padding(4.dp)
            .size(24.dp)
            .then(
              if (startRecordingCountdown) Modifier.withoutPlacement() else Modifier,
            ),
          imageVector = when (audioRecordingState) {
            AudioRecordingStepState.AudioRecording.NotRecording -> {
              HedvigIcons.Mic
            }

            is AudioRecordingStepState.AudioRecording.Playback -> {
              val ready = audioPlayerState as? AudioPlayerState.Ready
              if (ready?.readyState is AudioPlayerState.Ready.ReadyState.Playing) {
                HedvigIcons.Pause
              } else {
                HedvigIcons.Play
              }
            }

            is AudioRecordingStepState.AudioRecording.Recording -> {
              HedvigIcons.Stop
            }
          },
          contentDescription = EmptyContentDescription,
          tint = if (!isEnabled) {
            HedvigTheme.colorScheme.fillTertiary
          } else {
            when (audioRecordingState) {
              AudioRecordingStepState.AudioRecording.NotRecording,
              is AudioRecordingStepState.AudioRecording.Recording,
              -> HedvigTheme.colorScheme.fillWhite

              is AudioRecordingStepState.AudioRecording.Playback -> HedvigTheme.colorScheme.fillNegative
            }
          },
        )
        // Sized by the icon alone. The countdown's line box is taller than the icon once the text scale
        // passes ~1.5, and letting it size the container puts this button's label out of line with the
        // labels either side of it.
        Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
          HedvigText(
            text = if (startRecordingCountdown) countDownText else "",
            color = when (audioRecordingState) {
              AudioRecordingStepState.AudioRecording.NotRecording,
              is AudioRecordingStepState.AudioRecording.Recording,
              -> HedvigTheme.colorScheme.fillWhite

              is AudioRecordingStepState.AudioRecording.Playback -> HedvigTheme.colorScheme.fillNegative
            },
            modifier = Modifier.semantics {
              liveRegion = LiveRegionMode.Assertive
            },
          )
        }
      }
      Spacer(Modifier.height(4.dp))
      HedvigText(
        text = onClickLabel,
        fontSize = HedvigTheme.typography.label.fontSize,
        fontStyle = HedvigTheme.typography.label.fontStyle,
        color = if (isEnabled) HedvigTheme.colorScheme.textPrimary else HedvigTheme.colorScheme.textTertiary,
        modifier = Modifier.semantics {
          if (hideFromA11y) hideFromAccessibility()
        },
      )
    }
  }
}

@Composable
private fun SendButton(onSend: () -> Unit, isEnabled: Boolean, modifier: Modifier = Modifier) {
  Surface(
    shape = HedvigTheme.shapes.cornerLarge,
    modifier = modifier.clip(HedvigTheme.shapes.cornerLarge).semantics(true) {
      role = Role.Button
    }.clickable(
      enabled = isEnabled,
      onClick = onSend,
    ),
  ) {
    Column(
      modifier = Modifier.padding(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
        modifier = Modifier.clip(HedvigTheme.shapes.cornerXXLarge).background(
          color = if (!isEnabled) {
            HedvigTheme.colorScheme.surfaceSecondaryTransparent
          } else {
            HedvigTheme.colorScheme.signalBlueElement
          },
        ),
      ) {
        Icon(
          modifier = Modifier.padding(4.dp).size(24.dp),
          imageVector = HedvigIcons.ArrowUp,
          contentDescription = EmptyContentDescription,
          tint = if (!isEnabled) {
            HedvigTheme.colorScheme.fillTertiary
          } else {
            HedvigTheme.colorScheme.fillNegative
          },
        )
      }
      Spacer(Modifier.height(4.dp))
      HedvigText(
        text = stringResource(Res.string.AUDIO_RECORDER_SEND),
        fontSize = HedvigTheme.typography.label.fontSize,
        fontStyle = HedvigTheme.typography.label.fontStyle,
        color = if (isEnabled) HedvigTheme.colorScheme.textPrimary else HedvigTheme.colorScheme.textTertiary,
      )
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
  continueButtonLoading: Boolean,
  errorType: FreeTextErrorType?,
  canSubmit: Boolean,
  modifier: Modifier = Modifier,
) {
  val focusManager = LocalFocusManager.current
  Column(modifier) {
    if (isCurrentStep) {
      FreeTextDisplay(
        onClick = {
          focusManager.clearFocus()
          onLaunchFullScreenEditText()
        },
        freeTextValue = freeText,
        freeTextPlaceholder = stringResource(Res.string.CLAIMS_TEXT_INPUT_PLACEHOLDER),
        supportingText = when (errorType) {
          is FreeTextErrorType.TooShort -> {
            stringResource(
              Res.string.CLAIMS_TEXT_INPUT_MIN_CHARACTERS_ERROR,
              errorType.minLength,
            )
          }

          else -> {
            null
          }
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
      val description = stringResource(Res.string.TALKBACK_CLAIM_CHAT_YOUR_ANSWER) + freeText

      if (freeText != null) {
        RoundCornersPill(
          modifier = Modifier.fillMaxWidth()
            .padding(start = 48.dp)
            .wrapContentWidth(Alignment.End)
            .clearAndSetSemantics {
              contentDescription = description
            },
        ) {
          HedvigText(freeText, textAlign = TextAlign.End)
        }
      } else {
        SkippedLabel()
      }
    }
  }
}

private data class WaveState(
  val minFraction: Float,
  val maxFraction: Float,
  private val withRandomInitialValue: Boolean,
) {
  val animatable = Animatable(
    if (withRandomInitialValue) randomAroundFraction(1f) else 0f,
  )

  fun randomAroundFraction(fraction: Float): Float {
    val smallAdjustment = Random.nextDouble(-0.6, 0.0).toFloat()
    return lerp(
      minFraction,
      maxFraction,
      (fraction + (smallAdjustment * fraction)).coerceIn(0f, 1f),
    )
  }
}

@Composable
private fun AudioWaves(
  isRecording: Boolean,
  progressPercentage: ProgressPercentage?,
  modifier: Modifier = Modifier,
  amplitudes: List<Int>? = null,
) {
  val playedColor = LocalContentColor.current
  val notPlayedColor = LocalContentColor.current.copy(0.38f).compositeOver(HedvigTheme.colorScheme.surfacePrimary)
  val fixedColor = fixedRestingColor
  val density = LocalDensity.current
  val strokeWidthPx = with(density) { WAVE_WIDTH.toPx() }
  val updatedAmplitudes by rememberUpdatedState(amplitudes)

  var numberOfWaves by remember { mutableStateOf(0) }

  val waveStates = remember(numberOfWaves) {
    List(numberOfWaves) { waveIndex ->
      val wavePosition = waveIndex + 1
      val centerStart = numberOfWaves * 0.2f
      val centerEnd = numberOfWaves * 0.8f

      // Calculate fade multiplier: 0 at edges, 1 in center (60%)
      val fadeMultiplier = when {
        wavePosition <= centerStart -> wavePosition / centerStart
        wavePosition >= centerEnd -> (numberOfWaves - wavePosition) / centerStart
        else -> 1f
      }

      val minWaveHeightFraction = 0f
      val maxWaveHeightFraction = 1f
      WaveState(
        minFraction = minWaveHeightFraction,
        maxFraction = maxWaveHeightFraction * fadeMultiplier,
        withRandomInitialValue = updatedAmplitudes == null,
      )
    }
  }

  if (isRecording && waveStates.isNotEmpty()) {
    LaunchedEffect(waveStates) {
      while (isActive) {
        waveStates.map { waveState ->
          async {
            val maxWaveHeightFraction = getCurrentAmplitudePercentage(updatedAmplitudes.orEmpty())
            waveState.animatable.animateTo(
              targetValue = waveState.randomAroundFraction(maxWaveHeightFraction),
              animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing),
            )
          }
        }.awaitAll()
      }
    }
  }

  Canvas(modifier) {
    val calculatedNumberOfWaves = (size.width / with(density) { (WAVE_WIDTH + WAVE_SPACING).toPx() }).toInt()
    if (numberOfWaves != calculatedNumberOfWaves) {
      numberOfWaves = calculatedNumberOfWaves
      return@Canvas
    }

    val centerY = size.height / 2f
    val spacing = size.width / (numberOfWaves - 1)

    waveStates.forEachIndexed { waveIndex, waveState ->
      val heightPercentage = waveState.animatable.value

      val color = if (progressPercentage != null) {
        val hasPlayedThisWave = progressPercentage.value * numberOfWaves > waveIndex
        if (hasPlayedThisWave) playedColor else notPlayedColor
      } else {
        fixedColor
      }

      val lineHeight = WAVE_MAX_HEIGHT.toPx() * heightPercentage
      val x = waveIndex * spacing
      val startY = centerY - lineHeight / 2f
      val endY = centerY + lineHeight / 2f

      drawLine(
        color = color,
        start = Offset(x, startY),
        end = Offset(x, endY),
        strokeWidth = strokeWidthPx,
        cap = StrokeCap.Round,
      )
    }
  }
}

private val fixedRestingColor: Color
  @Composable
  get() = HedvigTheme.colorScheme.fillPrimary.copy(alpha = 0.6f)

private fun getCurrentAmplitudePercentage(amplitudes: List<Int>): Float {
  if (amplitudes.size <= 10) return 0f
  val lowerCap = 80
  val higherCap = 1000
  val currentAmplitude = amplitudes.last().coerceIn(lowerCap, higherCap)
  val min = amplitudes.min().coerceAtLeast(lowerCap)
  val max = amplitudes.max().coerceAtMost(higherCap)

  if (max == min && max == currentAmplitude) {
    return when (currentAmplitude) {
      lowerCap -> 0f
      higherCap -> 1f
      else -> 0.5f
    }
  }
  val minimumAmplitudeThreshold = 200
  val minimumDynamicRange = 50

  val tooSmallDynamicRange = max - min < minimumDynamicRange
  val isVeryQuiet = max < minimumAmplitudeThreshold

  fun calculateRange(current: Int, lowerCap: Int, maxCap: Int): Float {
    return ((current.toFloat() - lowerCap.toFloat()) / (maxCap.toFloat() - lowerCap.toFloat()))
  }
  return when {
    tooSmallDynamicRange && isVeryQuiet -> 0f

    // If the maximum amplitude is too low (quiet environment), use absolute scaling
    isVeryQuiet -> calculateRange(currentAmplitude, lowerCap, minimumAmplitudeThreshold).coerceIn(0f, 0.3f)

    else -> calculateRange(currentAmplitude, min, max).coerceIn(0f, 1f)
  }
}

@Composable
fun RestingAudioPlayer(modifier: Modifier = Modifier) {
  val color = fixedRestingColor
  val density = LocalDensity.current
  val strokeWidthPx = with(density) { WAVE_WIDTH.toPx() }

  Canvas(modifier) {
    val numberOfWaves = (size.width / with(density) { (WAVE_WIDTH + WAVE_SPACING).toPx() }).toInt()
    val spacing = size.width / (numberOfWaves - 1)
    val centerY = size.height / 2f

    repeat(numberOfWaves) { waveIndex ->
      val x = waveIndex * spacing
      drawLine(
        color = color,
        start = Offset(x, centerY),
        end = Offset(x, centerY),
        strokeWidth = strokeWidthPx,
        cap = StrokeCap.Round,
      )
    }
  }
}

// Width the close button drawn over the card's corner needs kept clear of it.
private val CLOSE_BUTTON_CLEARANCE = 32.dp

// Below this the band cannot hold enough bars to read as a waveform, so it is dropped instead.
private val MINIMUM_WAVE_BAND_WIDTH = 160.dp

// Past this the band stops reading as a waveform and starts reading as a rule drawn across the card. No
// phone reaches it in either arrangement, the widest being a large phone in landscape at about 520dp, so
// it only takes effect on the screens that have width to spare: tablets and unfolded foldables.
private val MAXIMUM_WAVE_BAND_WIDTH = 560.dp

// A window shorter than this shows the card's pieces along the free width instead of stacked.
private val SHORT_WINDOW_MAX_HEIGHT = 480.dp

// The band is only as tall as WAVE_MAX_HEIGHT, so its insets are what give it air. A taller state, the
// error or the spinner, grows the band rather than being boxed into a fixed height.
private val WAVE_BAND_HORIZONTAL_INSET = 24.dp
private val WAVE_BAND_ROW_INSET = 8.dp
private val WAVE_BAND_VERTICAL_INSET = 24.dp

// The field grows with the answer to this many lines and then scrolls inside itself.
private const val TEXT_ANSWER_MAX_LINES = 6

private val WAVE_WIDTH = 2.dp
private val WAVE_SPACING = 3.dp
private val WAVE_MIN_HEIGHT = 2.dp
private val WAVE_MAX_HEIGHT = 30.dp

@HedvigPreview
@Composable
private fun PreviewAudioRecordingSheetContent(
  @PreviewParameter(AudioRecordingSheetContentStateProvider::class)
  state: AudioRecordingStepState.AudioRecording,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AudioRecordingSheetContent(
        clock = Clock.System,
        submitAudioFile = {},
        redo = {},
        isSubmitting = false,
        audioPlayer = null,
        audioRecordingState = state,
        stopRecording = {},
        startRecording = {},
        recordAudioPermissionState = MockPermissionState(granted = true),
        isShortWindow = false,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewShortWindowAudioRecordingSheetContent(
  @PreviewParameter(AudioRecordingSheetContentStateProvider::class)
  state: AudioRecordingStepState.AudioRecording,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AudioRecordingSheetContent(
        clock = Clock.System,
        submitAudioFile = {},
        redo = {},
        isSubmitting = false,
        audioPlayer = null,
        audioRecordingState = state,
        stopRecording = {},
        startRecording = {},
        recordAudioPermissionState = MockPermissionState(granted = true),
        isShortWindow = true,
      )
    }
  }
}

private class AudioRecordingSheetContentStateProvider :
  CollectionPreviewParameterProvider<AudioRecordingStepState.AudioRecording>(
    listOf(
      AudioRecordingStepState.AudioRecording.NotRecording,
      AudioRecordingStepState.AudioRecording.Recording(
        amplitudes = listOf(100, 200, 150, 300),
        startedAt = Clock.System.now(),
        filePath = "/path/to/recording.mp4",
      ),
      AudioRecordingStepState.AudioRecording.Playback(
        audioPath = AudioPath.FilePath("/path/to/recording.mp4"),
        isPlaying = false,
        isPrepared = true,
        hasError = false,
      ),
      AudioRecordingStepState.AudioRecording.Playback(
        audioPath = AudioPath.FilePath("/path/to/recording.mp4"),
        isPlaying = false,
        isPrepared = false,
        hasError = false,
      ),
      AudioRecordingStepState.AudioRecording.Playback(
        audioPath = AudioPath.FilePath("/path/to/recording.mp4"),
        isPlaying = false,
        isPrepared = false,
        hasError = true,
      ),
    ),
  )

private class MockPermissionState(val granted: Boolean) : PermissionState {
  override val permission: String = "android.permission.RECORD_AUDIO"
  override val status: PermissionStatus = if (granted) PermissionStatus.Granted else PermissionStatus.Denied(false)

  override fun launchPermissionRequest() {}
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
