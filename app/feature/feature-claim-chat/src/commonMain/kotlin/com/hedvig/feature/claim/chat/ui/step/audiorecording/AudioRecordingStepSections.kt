package com.hedvig.feature.claim.chat.ui.step.audiorecording

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.audioplayer.rememberAudioPlayer
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.compose.ui.withoutPlacement
import com.hedvig.android.core.uidata.DecimalFormatter
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.PermissionDialog
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.freetext.FreeTextDisplay
import com.hedvig.android.design.system.hedvig.icon.ArrowUp
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Mic
import com.hedvig.android.design.system.hedvig.icon.Pause
import com.hedvig.android.design.system.hedvig.icon.Play
import com.hedvig.android.design.system.hedvig.icon.Reload
import com.hedvig.android.design.system.hedvig.icon.Stop
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.show
import com.hedvig.audio.player.data.AudioPlayer
import com.hedvig.audio.player.data.AudioPlayerState
import com.hedvig.audio.player.data.PlayableAudioSource
import com.hedvig.audio.player.data.ProgressPercentage
import com.hedvig.feature.claim.chat.ClaimChatEvent
import com.hedvig.feature.claim.chat.FreeTextRestrictions
import com.hedvig.feature.claim.chat.data.AudioRecordingStepState
import com.hedvig.feature.claim.chat.data.ClaimIntentStep
import com.hedvig.feature.claim.chat.data.FreeTextErrorType
import com.hedvig.feature.claim.chat.data.StepContent
import com.hedvig.feature.claim.chat.ui.common.EditButton
import com.hedvig.feature.claim.chat.ui.common.RoundCornersPill
import com.hedvig.feature.claim.chat.ui.common.SkippedLabel
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
import hedvig.resources.TALKBACK_RECORDING_DURATION
import hedvig.resources.TALKBACK_RECORDING_NOW
import hedvig.resources.claims_skip_button
import kotlin.math.abs
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
  freeText: String?,
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
      canSkip = stepContent.isSkippable,
      onSkip = onSkip,
      isCurrentStep = isCurrentStep,
      freeText = freeText,
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
    modifier = modifier,
  ) { recordingState ->
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      when (recordingState) {
        is AudioRecordingStepState.FreeTextDescription -> {
          FreeTextInputSection(
            submitFreeText = submitFreeText,
            showAudioRecording = onSwitchToAudioRecording,
            onLaunchFullScreenEditText = onLaunchFullScreenEditText,
            freeText = freeText,
            hasError = recordingState.hasError,
            errorType = recordingState.errorType,
            isCurrentStep = isCurrentStep,
            continueButtonLoading = continueButtonLoading,
            canSubmit = recordingState.canSubmit,
          )
        }

        is AudioRecordingStepState.AudioRecording -> {
          Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val state = rememberHedvigBottomSheetState<Unit>()
            if (isCurrentStep) {
              AudioRecordingBottomSheet(
                audioRecordingState = recordingState,
                clock = clock,
                shouldShowRequestPermissionRationale = onShouldShowRequestPermissionRationale,
                startRecording = startRecording,
                stopRecording = stopRecording,
                submitAudioFile = submitAudioFile,
                redo = redoRecording,
                openAppSettings = openAppSettings,
                continueButtonLoading = continueButtonLoading,
                bottomSheetState = state,
              )
              HedvigButton(
                enabled = true,
                text = stringResource(Res.string.CLAIM_CHAT_USE_AUDIO),
                onClick = state::show,
                modifier = Modifier.fillMaxWidth(),
              )
              if (freeTextAvailable) {
                HedvigButton(
                  enabled = true,
                  buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
                  text = stringResource(Res.string.CLAIM_CHAT_USE_TEXT_INPUT),
                  onClick = onSwitchToFreeText,
                  modifier = Modifier.fillMaxWidth(),
                )
              }
            } else {
              if (recordingState is AudioRecordingStepState.AudioRecording.Playback) {
                val audioPlayer = rememberAudioPlayer(
                  PlayableAudioSource.LocalFilePath(recordingState.filePath),
                )
                HedvigAudioPlayer(
                  audioPlayer = audioPlayer,
                  Modifier.padding(start = 45.dp),
                )
              } else {
                SkippedLabel()
              }
            }
          }
        }
      }

      if (canSkip && isCurrentStep) {
        HedvigButton(
          stringResource(Res.string.claims_skip_button),
          onClick = onSkip,
          isLoading = skipButtonLoading,
          enabled = !skipButtonLoading,
          modifier = Modifier.fillMaxWidth(),
          buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        )
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

  val audioPlayer = (audioRecordingState as? AudioRecordingStepState.AudioRecording.Playback)?.let {
    rememberAudioPlayer(
      PlayableAudioSource.LocalFilePath(it.filePath),
    )
  }

  LaunchedEffect(bottomSheetState.isVisible) {
    if (!bottomSheetState.isVisible) {
      stopRecording()
    }
  }
  LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
    stopRecording()
  }
  HedvigBottomSheet(bottomSheetState, modifier) {
    Column {
      HedvigText(
        stringResource(Res.string.CLAIM_TRIAGING_TITLE),
        modifier = Modifier.fillMaxWidth().semantics {
          heading()
        },
        textAlign = TextAlign.Center,
      )
      DynamicClock(audioRecordingState, clock, audioPlayer)
      Spacer(Modifier.height(24.dp))

      AnimatedContent(
        targetState = audioRecordingState,
        transitionSpec = {
          fadeIn(animationSpec = tween(300)).togetherWith(fadeOut(animationSpec = tween(300)))
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
          modifier = Modifier.height(158.dp).fillMaxWidth().padding(horizontal = 45.dp),
          contentAlignment = Alignment.Center,
          propagateMinConstraints = true,
        ) {
          when (target) {
            is AudioRecordingStepState.AudioRecording.Playback if !target.isPrepared -> {
              HedvigCircularProgressIndicator(Modifier.wrapContentSize())
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
      Row(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
      ) {
        StartOverButton(
          modifier = Modifier.weight(1f),
          onStartOver = redo,
          isEnabled = audioRecordingState is AudioRecordingStepState.AudioRecording.Playback && !continueButtonLoading,
        )
        Spacer(Modifier.width(4.dp))
        ControlButton(
          modifier = Modifier.weight(1f),
          audioPlayer = audioPlayer,
          onStartRecording = {
            when (recordAudioPermissionState.status) {
              PermissionStatus.Granted -> startRecording()
              is PermissionStatus.Denied -> recordAudioPermissionState.launchPermissionRequest()
            }
          },
          onStopRecording = stopRecording,
          audioRecordingState = audioRecordingState,
          isEnabled = !continueButtonLoading,
        )
        Spacer(Modifier.width(4.dp))
        SendButton(
          modifier = Modifier.weight(1f),
          onSend = submitAudioFile,
          isEnabled = audioRecordingState is AudioRecordingStepState.AudioRecording.Playback && !continueButtonLoading,
        )
      }
      Spacer(Modifier.height(16.dp))
      Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
    }
  }
}

@Composable
private fun DynamicClock(
  audioRecordingState: AudioRecordingStepState.AudioRecording,
  clock: Clock,
  audioPlayer: AudioPlayer?,
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

  val durationDescription = timerState?.let {
    stringResource(Res.string.TALKBACK_RECORDING_DURATION, it)
  }

  if (timerState != null) {
    Box(
      Modifier.fillMaxWidth().clearAndSetSemantics {
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
        text = timerState.minutes,
        modifier = Modifier.requiredWidth(0.dp).align(Alignment.CenterStart).wrapContentWidth(Alignment.End, true),
        color = HedvigTheme.colorScheme.textSecondary,
      )
      HedvigText(
        text = timerState.seconds,
        modifier = Modifier.requiredWidth(0.dp).align(Alignment.CenterEnd).wrapContentWidth(Alignment.Start, true),
        color = HedvigTheme.colorScheme.textSecondary,
      )
    }
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

  val recordingStateDescription = when (audioRecordingState) {
    is AudioRecordingStepState.AudioRecording.Recording -> stringResource(Res.string.TALKBACK_RECORDING_NOW)
    else -> ""
  }

  var countDownText by remember { mutableStateOf("3") }
  var startRecordingCountdown by remember { mutableStateOf(false) }

  val lifecycleOwner = LocalLifecycleOwner.current
  LaunchedEffect(startRecordingCountdown, lifecycleOwner) {
    if (startRecordingCountdown) {
      delay(1000)
      countDownText = "2"
      delay(1000)
      countDownText = "1"
      delay(1000)
      if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        onStartRecording()
      }
      startRecordingCountdown = false
    }
    countDownText = "3"
  }

  Surface(
    shape = HedvigTheme.shapes.cornerLarge,
    modifier = modifier.clip(HedvigTheme.shapes.cornerLarge).semantics {
      stateDescription = recordingStateDescription
    }.clickable(
      enabled = isEnabled,
      onClickLabel = onClickLabel,
      role = Role.Button,
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
        modifier = Modifier.clip(HedvigTheme.shapes.cornerXXLarge).background(
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
          modifier = Modifier.padding(4.dp).size(24.dp).then(
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
        if (startRecordingCountdown) {
          HedvigText(
            text = countDownText,
            color = when (audioRecordingState) {
              AudioRecordingStepState.AudioRecording.NotRecording,
              is AudioRecordingStepState.AudioRecording.Recording,
                -> HedvigTheme.colorScheme.fillWhite

              is AudioRecordingStepState.AudioRecording.Playback -> HedvigTheme.colorScheme.fillNegative
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
  Column(modifier) {
    if (isCurrentStep) {
      FreeTextDisplay(
        onClick = { onLaunchFullScreenEditText() },
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
      if (freeText != null) {
        RoundCornersPill(
          onClick = null,
          modifier = Modifier.fillMaxWidth().padding(start = 48.dp).wrapContentWidth(Alignment.End),
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
    if (withRandomInitialValue) randomAroundFraction(1f) else 0f
  )

  fun randomAroundFraction(fraction: Float): Float {
    val smallAdjustment = Random.nextDouble(-0.15, 0.15).toFloat()
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
  amplitudes: List<Int> = emptyList(),
) {
  val playedColor = LocalContentColor.current
  val notPlayedColor = LocalContentColor.current.copy(0.38f).compositeOver(HedvigTheme.colorScheme.surfacePrimary)
  val fixedColor = HedvigTheme.colorScheme.fillPrimary.copy(alpha = 0.6f)
  val density = LocalDensity.current
  val strokeWidthPx = with(density) { WAVE_WIDTH.toPx() }
  val updatedAmplitudes by rememberUpdatedState(amplitudes)

  var numberOfWaves by remember { mutableStateOf(0) }

  val waveStates = remember(numberOfWaves) {
    List(numberOfWaves) { waveIndex ->
      val wavePosition = waveIndex + 1
      val centerPoint = numberOfWaves / 2
      val distanceFromCenterPoint = abs(centerPoint - wavePosition)
      val percentageToCenterPoint =
        ((centerPoint - distanceFromCenterPoint).toFloat() / centerPoint)
      val minWaveHeightFraction = 0f
      val maxWaveHeightFraction = 1f
      WaveState(
        minWaveHeightFraction * percentageToCenterPoint,
        maxWaveHeightFraction * percentageToCenterPoint,
        if (amplitudes.isEmpty()) true else false,
      )
    }
  }

  if (isRecording && waveStates.isNotEmpty()) {
    LaunchedEffect(waveStates) {
      while (isActive) {
        waveStates.map { waveState ->
          async {
            val maxWaveHeightFraction = getCurrentAmplitudePercentage(updatedAmplitudes)
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

    waveStates.forEachIndexed { waveIndex, waveState ->
      val heightPercentage = waveState.animatable.value

      val color = if (progressPercentage != null) {
        val hasPlayedThisWave = progressPercentage.value * numberOfWaves > waveIndex
        if (hasPlayedThisWave) playedColor else notPlayedColor
      } else {
        fixedColor
      }

      val lineHeight = WAVE_MAX_HEIGHT.toPx() * heightPercentage
      val x = waveIndex * (WAVE_WIDTH + WAVE_SPACING).toPx()
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

private fun getCurrentAmplitudePercentage(amplitudes: List<Int>): Float {
  if (amplitudes.size < 5) return 0f
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
  return ((currentAmplitude - min) / (max.toFloat() - min)).coerceIn(0f..1f)
}

@Composable
fun RestingAudioPlayer(modifier: Modifier = Modifier) {
  val color = HedvigTheme.colorScheme.fillPrimary
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

private val WAVE_WIDTH = 2.dp
private val WAVE_SPACING = 3.dp
private val WAVE_MIN_HEIGHT = 2.dp
private val WAVE_MAX_HEIGHT = 30.dp

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
