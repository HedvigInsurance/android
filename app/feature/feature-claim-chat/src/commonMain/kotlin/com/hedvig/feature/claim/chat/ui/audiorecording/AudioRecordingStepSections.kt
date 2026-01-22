package com.hedvig.feature.claim.chat.ui.audiorecording

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.audioplayer.rememberAudioPlayer
import com.hedvig.android.compose.ui.EmptyContentDescription
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
import com.hedvig.android.logger.logcat
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
import hedvig.resources.CLAIM_TRIAGING_TITLE
import hedvig.resources.PERMISSION_DIALOG_RECORD_AUDIO_MESSAGE
import hedvig.resources.Res
import hedvig.resources.SAVE_AND_CONTINUE_BUTTON_LABEL
import hedvig.resources.TALKBACK_RECORDING_DURATION
import hedvig.resources.claims_skip_button
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.delay
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
                onClick = {
                  state.show(Unit)
                },
                modifier = Modifier.fillMaxWidth(),
              )
              if (freeTextAvailable) {
                Spacer(Modifier.height(8.dp))
                HedvigButton(
                  enabled = true,
                  buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
                  text = stringResource(Res.string.CLAIMS_USE_TEXT_INSTEAD),
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
                  Modifier.padding(
                    start = 45.dp,
                  ),
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

  val audioPlayer = (audioRecordingState as?
    AudioRecordingStepState.AudioRecording.Playback)?.let {
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
      DynamicClock(audioRecordingState, clock)
      Spacer(Modifier.height(24.dp))

      AnimatedContent(
        targetState = audioRecordingState,
        transitionSpec = {
      (fadeIn(animationSpec = tween(220, delayMillis = 90))
        .togetherWith(fadeOut(animationSpec = tween(90))))
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
          modifier = Modifier.height(158.dp),
          contentAlignment = Alignment.Center,
        ) {
          when (target) {
            is AudioRecordingStepState.AudioRecording.Playback if !target.isPrepared -> {
              HedvigCircularProgressIndicator()
            }

            is AudioRecordingStepState.AudioRecording.Playback -> {
              val audioPlayerState by audioPlayer?.audioPlayerState?.collectAsStateWithLifecycle()
                ?: remember { mutableStateOf(null) }
              if (audioPlayerState is AudioPlayerState.Ready) {
                AudioWaves(
                  animated = false,
                  progressPercentage = (audioPlayerState as AudioPlayerState.Ready).progressPercentage,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                      horizontal = 45.dp,
                      vertical = 29.dp,
                    ),
                )
              }
            }

            is AudioRecordingStepState.AudioRecording.Recording -> {
              AudioWaves(
                animated = true,
                progressPercentage = null,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(
                    horizontal = 45.dp,
                    vertical = 29.dp,
                  ),
              )
            }

            else -> {
              RestingAudioPlayer(
                Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 45.dp),
              )
            }
          }
        }
      }
      Row(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
      ) {
        AudioButton(
          modifier = Modifier.weight(1f),
          type = AudioButtonType.StartOver(
            onStartOver = redo,
            isEnabled = audioRecordingState is AudioRecordingStepState.AudioRecording.Playback
              && !continueButtonLoading,
          ),
          audioPlayer = null,
        )
        Spacer(Modifier.width(4.dp))
        AudioButton(
          modifier = Modifier.weight(1f),
          audioPlayer = audioPlayer,
          type = AudioButtonType.Control(
            onStartRecording = startRecording,
            onStopRecording = stopRecording,
            audioRecordingState = audioRecordingState,
            isEnabled = !continueButtonLoading,
          ),
        )
        Spacer(Modifier.width(4.dp))
        AudioButton(
          modifier = Modifier.weight(1f),
          type = AudioButtonType.Send(
            onSend = submitAudioFile,
            isEnabled = audioRecordingState is AudioRecordingStepState.AudioRecording.Playback
              && !continueButtonLoading,
          ),
          audioPlayer = null,
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
) {
  val startedRecordingAt by remember {
    mutableStateOf<Instant?>(null)
  }.apply {
    if (audioRecordingState is AudioRecordingStepState.AudioRecording.Recording) {
      value = audioRecordingState.startedAt
    }
  }

  val twoDigitsFormat = remember { DecimalFormatter("00") }
  val label = if (audioRecordingState is AudioRecordingStepState.AudioRecording.Recording) {
    val diff = clock.now() - (startedRecordingAt ?: clock.now())
    "${
      twoDigitsFormat.format(
        diff.inWholeMinutes,
      )
    }:${twoDigitsFormat.format(diff.inWholeSeconds % 60)}"
  } else null
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
private fun AudioButton(
  type: AudioButtonType,
  audioPlayer: AudioPlayer?,
  modifier: Modifier = Modifier,
) {
  val audioPlayerState by audioPlayer?.audioPlayerState?.collectAsStateWithLifecycle()
    ?: remember { mutableStateOf<AudioPlayerState?>(null) }
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
          logcat { "Mariia: surface clicked, type: $type" }
          when (type) {
            is AudioButtonType.Control -> when (type.audioRecordingState) {
              AudioRecordingStepState.AudioRecording.NotRecording -> {
                logcat { "Mariia:  type.onStartRecording clicked" }
                type.onStartRecording()
              }

              is AudioRecordingStepState.AudioRecording.Playback -> {
                val ready = audioPlayerState as? AudioPlayerState.Ready
                if (ready?.readyState is AudioPlayerState.Ready.ReadyState.Playing) {
                  audioPlayer?.pausePlayer()
                } else {
                  audioPlayer?.startPlayer()
                }
              }

              is AudioRecordingStepState.AudioRecording.Recording -> type.onStopRecording()
            }

            is AudioButtonType.Send -> type.onSend()
            is AudioButtonType.StartOver -> type.onStartOver()
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
              is AudioButtonType.StartOver -> HedvigTheme.colorScheme.surfaceSecondaryTransparent
            },
          ),
      ) {
        Icon(
          modifier = Modifier.padding(4.dp).size(24.dp),
          imageVector = when (type) {
            is AudioButtonType.Control -> when (type.audioRecordingState) {
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
            }

            is AudioButtonType.Send -> HedvigIcons.ArrowUp
            is AudioButtonType.StartOver -> HedvigIcons.Reload
          },
          contentDescription = EmptyContentDescription,
          tint = if (!type.isEnabled) HedvigTheme.colorScheme.fillTertiary else {
            if (type is AudioButtonType.StartOver) HedvigTheme.colorScheme.fillPrimary else
              HedvigTheme.colorScheme.fillNegative
          },
        )
      }
      Spacer(Modifier.height(4.dp))
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
        fontSize = HedvigTheme.typography.label.fontSize,
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
    val audioRecordingState: AudioRecordingStepState.AudioRecording,
    override val isEnabled: Boolean,
  ) : AudioButtonType {
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

@Composable
private fun AudioWaves(
  animated: Boolean,
  progressPercentage: ProgressPercentage?,
  modifier: Modifier = Modifier,
) {
  val playedColor = LocalContentColor.current
  val notPlayedColor = LocalContentColor.current.copy(0.38f)
    .compositeOver(HedvigTheme.colorScheme.surfacePrimary)
  val fixedColor = HedvigTheme.colorScheme.fillPrimary.copy(alpha = 0.6f)

  BoxWithConstraints(modifier) {
    val numberOfWaves = remember(maxWidth) {
      (maxWidth / 5f).value.roundToInt()
    }
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth().height(maxHeight),
    ) {
      repeat(numberOfWaves) { waveIndex ->
        val baseHeight = remember(waveIndex, numberOfWaves) {
          val wavePosition = waveIndex + 1
          val centerPoint = numberOfWaves / 2
          val distanceFromCenterPoint = abs(centerPoint - wavePosition)
          val percentageToCenterPoint =
            ((centerPoint - distanceFromCenterPoint).toFloat() / centerPoint)
          val minWaveHeightFraction = 0.05f
          val maxWaveHeightFractionForSideWaves =  0.05f
          val maxWaveHeightFraction = 0.5f
          val maxHeightFraction = lerp(
            maxWaveHeightFractionForSideWaves,
            maxWaveHeightFraction,
            percentageToCenterPoint,
          )
          if (maxHeightFraction <= minWaveHeightFraction) {
            maxHeightFraction
          } else {
            Random.nextDouble(minWaveHeightFraction.toDouble(), maxHeightFraction.toDouble())
              .toFloat()
          }
        }

        val height = if (animated) {
          var animatedHeight by remember { mutableStateOf(baseHeight) }

          LaunchedEffect(waveIndex) {
            while (true) {
              delay((50..150).random().toLong())
              val variation = Random.nextFloat() * 0.2f - 0.1f
              animatedHeight = (baseHeight + variation).coerceIn(0.1f, 0.4f)
            }
          }

          val smoothHeight by animateFloatAsState(
            targetValue = animatedHeight,
            animationSpec = tween(durationMillis = 200, easing = LinearEasing),
          )
          smoothHeight
        } else {
          baseHeight
        }

        val backgroundColor = if (progressPercentage != null) {
          val hasPlayedThisWave = remember(progressPercentage, numberOfWaves, waveIndex) {
            progressPercentage.value * numberOfWaves > waveIndex
          }
          if (hasPlayedThisWave) playedColor else notPlayedColor
        } else {
          fixedColor
        }

        WavePill(
          heightFraction = height,
          backgroundColor = backgroundColor,
        )
      }
    }
  }
}

@Composable
private fun WavePill(
  heightFraction: Float,
  backgroundColor: Color,
) {
  Box(
    modifier = Modifier
      .width(WAVE_WIDTH)
      .fillMaxHeight(fraction = heightFraction)
      .clip(CircleShape)
      .background(backgroundColor),
  )
}

@Composable
fun RestingAudioPlayer(modifier: Modifier = Modifier) {
  BoxWithConstraints(modifier) {
    val numberOfWaves = remember(maxWidth) {
      (maxWidth / 5f).value.roundToInt()
    }
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth(),
    ) {
      repeat(numberOfWaves) { _ ->
        Box(
          modifier = Modifier
            .size(WAVE_WIDTH)
            .clip(CircleShape)
            .background(HedvigTheme.colorScheme.fillPrimary),
        )
      }
    }
  }
}

private val WAVE_WIDTH = 2.dp

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
