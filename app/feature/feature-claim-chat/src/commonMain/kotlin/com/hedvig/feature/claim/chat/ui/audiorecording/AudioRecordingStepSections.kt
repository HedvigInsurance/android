package com.hedvig.feature.claim.chat.ui.audiorecording

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.util.lerp
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
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.show
import com.hedvig.audio.player.data.AudioPlayer
import com.hedvig.audio.player.data.AudioPlayerState
import com.hedvig.audio.player.data.PlayableAudioSource
import com.hedvig.audio.player.data.ProgressPercentage
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
  ) { uiStateAnimated ->
    Column {
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
            if (isCurrentStep) {
              AudioRecordingBottomSheet(
                audioRecordingState = uiStateAnimated,
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
                Spacer(Modifier.height(8.dp))
                HedvigButton(
                  enabled = true,
                  buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
                  text = stringResource(Res.string.CLAIM_CHAT_USE_TEXT_INPUT),
                  onClick = onSwitchToFreeText,
                  modifier = Modifier.fillMaxWidth(),
                )
              }
            } else {
              if (uiStateAnimated is AudioRecordingStepState.AudioRecording.Playback) {
                val audioPlayer = rememberAudioPlayer(
                  PlayableAudioSource.LocalFilePath(uiStateAnimated.filePath),
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
        Spacer(Modifier.height(8.dp))
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

  val audioPlayer = (
    audioRecordingState as?
      AudioRecordingStepState.AudioRecording.Playback
    )?.let {
      rememberAudioPlayer(
        PlayableAudioSource.LocalFilePath(it.filePath),
      )
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
          fadeIn(animationSpec = tween(300))
            .togetherWith(fadeOut(animationSpec = tween(300)))
        },
        contentKey = { state ->
          when (state) {
            is AudioRecordingStepState.AudioRecording.Playback -> {
              if (state.isPrepared) "playback" else "loading"
            }

            is AudioRecordingStepState.AudioRecording.Recording -> "recording"
            else -> "resting"
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
          isEnabled = audioRecordingState is AudioRecordingStepState.AudioRecording.Playback &&
            !continueButtonLoading,
        )
        Spacer(Modifier.width(4.dp))
        ControlButton(
          modifier = Modifier.weight(1f),
          audioPlayer = audioPlayer,
          onStartRecording = startRecording,
          onStopRecording = stopRecording,
          audioRecordingState = audioRecordingState,
          isEnabled = !continueButtonLoading,
        )
        Spacer(Modifier.width(4.dp))
        SendButton(
          modifier = Modifier.weight(1f),
          onSend = submitAudioFile,
          isEnabled = audioRecordingState is AudioRecordingStepState.AudioRecording.Playback &&
            !continueButtonLoading,
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

  val label = when (audioRecordingState) {
    is AudioRecordingStepState.AudioRecording.Recording -> {
      val diff = clock.now() - (startedRecordingAt ?: clock.now())
      "${twoDigitsFormat.format(diff.inWholeMinutes)}:${twoDigitsFormat.format(diff.inWholeSeconds % 60)}"
    }

    is AudioRecordingStepState.AudioRecording.Playback -> {
      val ready = audioPlayerState as? AudioPlayerState.Ready
      if (ready != null) {
        val durationSeconds = ready.durationMillis / 1000
        "${twoDigitsFormat.format(durationSeconds / 60)}:${twoDigitsFormat.format(durationSeconds % 60)}"
      } else {
        null
      }
    }

    else -> null
  }

  val durationDescription = label?.let {
    stringResource(
      Res.string.TALKBACK_RECORDING_DURATION,
      it,
    )
  }

  HedvigText(
    text = label ?: "",
    textAlign = TextAlign.Center,
    modifier = Modifier.fillMaxWidth().clearAndSetSemantics {
      if (durationDescription != null) {
        contentDescription = durationDescription
      }
    },
    color = HedvigTheme.colorScheme.textSecondary,
  )
}

@Composable
private fun StartOverButton(
  onStartOver: () -> Unit,
  isEnabled: Boolean,
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
        enabled = isEnabled,
        onClick = onStartOver,
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
  var isIconVisible by remember { mutableStateOf(true) }

  LaunchedEffect(isIconVisible) {
    if (!isIconVisible) {
      delay(1000)
      countDownText = "2"
      delay(1000)
      countDownText = "1"
      delay(1000)
      countDownText = "3"
      onStartRecording()
      isIconVisible = true
    }
  }

  Surface(
    shape = HedvigTheme.shapes.cornerLarge,
    modifier = modifier
      .clip(HedvigTheme.shapes.cornerLarge)
      .semantics {
        stateDescription = recordingStateDescription
      }
      .clickable(
        enabled = isEnabled,
        onClickLabel = onClickLabel,
        role = Role.Button,
        onClick = {
          when (audioRecordingState) {
            AudioRecordingStepState.AudioRecording.NotRecording -> {
              isIconVisible = false
            }

            is AudioRecordingStepState.AudioRecording.Playback -> {
              val ready = audioPlayerState as? AudioPlayerState.Ready
              if (ready?.readyState is AudioPlayerState.Ready.ReadyState.Playing) {
                audioPlayer?.pausePlayer()
              } else {
                audioPlayer?.startPlayer()
              }
            }

            is AudioRecordingStepState.AudioRecording.Recording -> onStopRecording()
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
        contentAlignment = Alignment.Center
      ) {

        Icon(
          modifier = Modifier
            .padding(4.dp)
            .size(24.dp)
            .then (
              if (isIconVisible) Modifier else Modifier.withoutPlacement()
            ),
          imageVector = when (audioRecordingState) {
            AudioRecordingStepState.AudioRecording.NotRecording -> HedvigIcons.Mic
            is AudioRecordingStepState.AudioRecording.Playback -> {
              val ready = audioPlayerState as? AudioPlayerState.Ready
              if (ready?.readyState is AudioPlayerState.Ready.ReadyState.Playing) {
                HedvigIcons.Pause
              } else {
                HedvigIcons.Play
              }
            }

            is AudioRecordingStepState.AudioRecording.Recording -> HedvigIcons.Pause
          },
          contentDescription = EmptyContentDescription,
          tint = if (!isEnabled) {
            HedvigTheme.colorScheme.fillTertiary
          } else {
            when (audioRecordingState) {
              AudioRecordingStepState.AudioRecording.NotRecording,
              is AudioRecordingStepState.AudioRecording.Recording -> HedvigTheme.colorScheme.fillWhite
              is AudioRecordingStepState.AudioRecording.Playback -> HedvigTheme.colorScheme.fillNegative
            }
          },
        )
        if (!isIconVisible) {
          HedvigText(text = countDownText, color = when (audioRecordingState) {
            AudioRecordingStepState.AudioRecording.NotRecording,
            is AudioRecordingStepState.AudioRecording.Recording -> HedvigTheme.colorScheme.fillWhite
            is AudioRecordingStepState.AudioRecording.Playback -> HedvigTheme.colorScheme.fillNegative
          })
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
private fun SendButton(
  onSend: () -> Unit,
  isEnabled: Boolean,
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
        enabled = isEnabled,
        onClick = onSend,
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

private data class WaveState(
  val minFraction: Float,
  val maxFraction: Float,
) {
  val animatable = Animatable(random())

  fun random(): Float = lerp(minFraction, maxFraction, Random.nextFloat())
}

@Composable
private fun AudioWaves(isRecording: Boolean, progressPercentage: ProgressPercentage?, modifier: Modifier = Modifier) {
  val playedColor = LocalContentColor.current
  val notPlayedColor = LocalContentColor.current.copy(0.38f)
    .compositeOver(HedvigTheme.colorScheme.surfacePrimary)
  val fixedColor = HedvigTheme.colorScheme.fillPrimary.copy(alpha = 0.6f)
  val density = LocalDensity.current
  val strokeWidthPx = with(density) { WAVE_WIDTH.toPx() }

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
      WaveState(minWaveHeightFraction * percentageToCenterPoint, maxWaveHeightFraction * percentageToCenterPoint)
    }
  }

  if (isRecording && waveStates.isNotEmpty()) {
    LaunchedEffect(waveStates) {
      while (isActive) {
        waveStates.map { waveState ->
          async {
            waveState.animatable.animateTo(
              targetValue = waveState.random(),
              animationSpec = tween(durationMillis = 200, easing = LinearEasing),
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
