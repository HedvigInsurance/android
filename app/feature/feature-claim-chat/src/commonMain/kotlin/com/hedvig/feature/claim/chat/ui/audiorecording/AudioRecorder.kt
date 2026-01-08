package com.hedvig.feature.claim.chat.ui.audiorecording

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.audioplayer.rememberAudioPlayer
import com.hedvig.android.core.uidata.DecimalFormatter
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.tokens.MotionTokens
import com.hedvig.audio.player.data.PlayableAudioSource
import com.hedvig.feature.claim.chat.data.AudioRecordingStepState
import com.hedvig.feature.claim.chat.ui.RoundCornersPill
import com.hedvig.feature.claim.chat.ui.SkippedLabel
import hedvig.resources.A11Y_AUDIO_RECORDING
import hedvig.resources.CLAIMS_USE_TEXT_INSTEAD
import hedvig.resources.CLAIM_CHAT_AUDIO_RECORDING_LABEL
import hedvig.resources.CLAIM_CHAT_FREE_TEXT_LABEL
import hedvig.resources.EMBARK_RECORD_AGAIN
import hedvig.resources.EMBARK_START_RECORDING
import hedvig.resources.EMBARK_STOP_RECORDING
import hedvig.resources.Res
import hedvig.resources.SAVE_AND_CONTINUE_BUTTON_LABEL
import hedvig.resources.TALKBACK_RECORDING_DURATION
import hedvig.resources.TALKBACK_RECORDING_NOW
import kotlin.math.sqrt
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AudioRecorder(
  uiState: AudioRecordingStepState.AudioRecording,
  startRecording: () -> Unit,
  clock: Clock,
  stopRecording: () -> Unit,
  submitAudioFile: () -> Unit,
  redo: () -> Unit,
  allowFreeText: Boolean,
  onLaunchFreeText: () -> Unit,
  isCurrentStep: Boolean,
  continueButtonLoading: Boolean,
  modifier: Modifier = Modifier,
) {
  when (uiState) {
    is AudioRecordingStepState.AudioRecording.Playback -> Playback(
      uiState = uiState,
      submit = submitAudioFile,
      redo = redo,
      modifier = modifier,
      isCurrentStep = isCurrentStep,
      continueButtonLoading = continueButtonLoading,
    )

    else -> {
      if (!isCurrentStep) {
        SkippedLabel()
      } else {
        val isRecording = uiState is AudioRecordingStepState.AudioRecording.Recording
        val isRecordingTransition = updateTransition(isRecording)
        if (isRecording) {
          ScreenOnFlag()
        }

        val startRecordingText = stringResource(Res.string.EMBARK_START_RECORDING)
        val stopRecordingText = stringResource(Res.string.EMBARK_STOP_RECORDING)
        val audioRecordingText = stringResource(Res.string.A11Y_AUDIO_RECORDING)
        val recordingState = stringResource(Res.string.TALKBACK_RECORDING_NOW)
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = modifier.fillMaxWidth(),
        ) {
          Spacer(Modifier.height(60.dp))
          Box(
            contentAlignment = Alignment.Center,
          ) {
            if (uiState is AudioRecordingStepState.AudioRecording.Recording && uiState.amplitudes.isNotEmpty()) {
              RecordingAmplitudeIndicator(amplitude = uiState.amplitudes.last())
            }
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .shadow(2.dp, CircleShape)
                .size(72.dp)
                .background(Color.White, CircleShape)
                .semantics {
                  contentDescription = audioRecordingText
                  stateDescription = if (isRecording) recordingState else ""
                }
                .clickable(
                  onClickLabel = when (isRecording) {
                    true -> stopRecordingText
                    // todo: this one is not working somehow,
                    // so added onClickLabel inside val recordingState
                    false -> startRecordingText
                  },
                  role = Role.Button,
                ) {
                  if (isRecording) {
                    stopRecording()
                  } else {
                    startRecording()
                  }
                },
            ) {
              val size by isRecordingTransition.animateDp(label = "sizeAnimation") { isRecording ->
                if (isRecording) 18.dp else 32.dp
              }
              val color by isRecordingTransition.animateColor(label = "colorAnimation") { isRecording ->
                if (isRecording) Color.Black else HedvigTheme.colorScheme.signalRedElement
              }
              val cornerRadius by isRecordingTransition.animateDp(label = "cornerRadiusAnimation") { isRecording ->
                if (isRecording) 2.dp else 16.dp
              }
              Box(
                Modifier.size(size).background(color, RoundedCornerShape(cornerRadius)),
              )
            }
          }
          Spacer(Modifier.height(24.dp))
          val startedRecordingAt by remember {
            mutableStateOf<Instant?>(null)
          }.apply {
            if (uiState is AudioRecordingStepState.AudioRecording.Recording) {
              value = uiState.startedAt
            }
          }
          val twoDigitsFormat = remember { DecimalFormatter("00") }
          isRecordingTransition.AnimatedContent(
            transitionSpec = {
              val animationSpec = tween<IntOffset>(MotionTokens.DurationLong1.toInt())
              val animationSpecFade = tween<Float>(MotionTokens.DurationMedium1.toInt())
              val animationSpecFloat = tween<Float>(MotionTokens.DurationLong1.toInt())
              val scale = 0.6f
              val enterTransition = slideInVertically(animationSpec) + fadeIn(animationSpecFade) + scaleIn(
                animationSpecFloat,
                initialScale = scale,
              )
              val exitTransition = slideOutVertically(animationSpec) + fadeOut(animationSpecFade) + scaleOut(
                animationSpecFloat,
                targetScale = scale,
              )
              enterTransition togetherWith exitTransition
            },
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxWidth(),
          ) { isRecording ->
            if (isRecording) {
              val diff = clock.now() - (startedRecordingAt ?: clock.now())
              val label =
                "${twoDigitsFormat.format(diff.inWholeMinutes)}:${twoDigitsFormat.format(diff.inWholeSeconds % 60)}"
              val durationDescription = stringResource(Res.string.TALKBACK_RECORDING_DURATION, label)
              HedvigText(
                text = label,
                style = HedvigTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp).clearAndSetSemantics {
                  contentDescription = durationDescription
                },
              )
            } else {
              if (allowFreeText) {
                HedvigTextButton(
                  text = stringResource(Res.string.CLAIMS_USE_TEXT_INSTEAD),
                  onClick = onLaunchFreeText,
                )
              } else {
                HedvigText(
                  text = startRecordingText,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.padding(bottom = 16.dp),
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun Playback(
  uiState: AudioRecordingStepState.AudioRecording.Playback,
  submit: () -> Unit,
  redo: () -> Unit,
  isCurrentStep: Boolean,
  continueButtonLoading: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
  ) {
    if (!uiState.isPrepared) {
      HedvigCircularProgressIndicator()
    } else {
      val audioPlayer = rememberAudioPlayer(PlayableAudioSource.LocalFilePath(uiState.filePath))
      if (!isCurrentStep) {
        VoiceRecordingLabel(
          labelType = AudioRecordingLabelType.AUDIO
        ) {
          HedvigAudioPlayer(
            audioPlayer = audioPlayer,
            modifier = Modifier.then(
              Modifier.padding(start = 48.dp),
            ),
          )
        }
      } else {
        HedvigAudioPlayer(
          audioPlayer = audioPlayer,
        )
      }
    }
    if (isCurrentStep) {
      HedvigButton(
        onClick = submit,
        text = stringResource(Res.string.SAVE_AND_CONTINUE_BUTTON_LABEL),
        isLoading = continueButtonLoading,
        enabled = !continueButtonLoading,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
      )
      HedvigTextButton(
        text = stringResource(Res.string.EMBARK_RECORD_AGAIN),
        onClick = redo,
        enabled = true,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
      )
    }
  }
}

@Composable
internal fun VoiceRecordingLabel(
  labelType: AudioRecordingLabelType,
  extendedContent: @Composable () -> Unit,
) {
  var showExtended by remember { mutableStateOf(false) }
  AnimatedContent(showExtended) { target ->
    if (target) {
      extendedContent()
    } else {
      Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth(),
      ) {
        RoundCornersPill(
          onClick = { showExtended = true },
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              HedvigIcons.Checkmark, null,
              tint = HedvigTheme.colorScheme.signalGreenElement,
            )
            Spacer(Modifier.width(4.dp))
            HedvigText(
              text = when (labelType) {
                AudioRecordingLabelType.TEXT -> stringResource(Res.string.CLAIM_CHAT_FREE_TEXT_LABEL)
                AudioRecordingLabelType.AUDIO ->  stringResource(Res.string.CLAIM_CHAT_AUDIO_RECORDING_LABEL)
              }
            )
          }
        }
      }
    }
  }
}

internal enum class AudioRecordingLabelType {
  TEXT,
  AUDIO
}

/**
 * @param amplitude A value typically coming from `MediaRecorder.getMaxAmplitude()` which usually ranges from 0-4000,
 *  and can peak to ~20000. Gets transformed to a more reasonable value of 0-1000 for the circle radius.
 */
@Composable
fun RecordingAmplitudeIndicator(amplitude: Int, modifier: Modifier = Modifier) {
  val color = LocalContentColor.current.copy(alpha = 0.12f)
  val animated by animateIntAsState(
    targetValue = (sqrt(amplitude.toDouble()).toInt() * 10).coerceAtMost(300),
    animationSpec = spring(
      stiffness = Spring.StiffnessLow,
    ),
    label = "recordingAmplitudeIndicator",
  )

  Canvas(modifier = modifier) {
    drawCircle(
      color = color,
      radius = animated.toFloat(),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewRecordingAmplitudeIndicator() {
  val infiniteTransition = rememberInfiniteTransition()
  val amplitude by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 4000f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "Amplitude value",
  )
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      RecordingAmplitudeIndicator(amplitude.toInt(), Modifier.fillMaxSize())
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewNotRecording() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AudioRecorder(
        uiState = AudioRecordingStepState.AudioRecording.NotRecording,
        startRecording = {},
        clock = Clock.System,
        stopRecording = {},
        submitAudioFile = {},
        redo = {},
        allowFreeText = true,
        onLaunchFreeText = {},
        isCurrentStep = true,
        continueButtonLoading = false,
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewRecording() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      AudioRecorder(
        uiState = AudioRecordingStepState.AudioRecording.Recording(
          listOf(70),
          Clock.System.now().minus(1019.seconds),
          "",
        ),
        startRecording = { },
        clock = Clock.System,
        stopRecording = { },
        submitAudioFile = {},
        allowFreeText = false,
        onLaunchFreeText = {},
        redo = { },
        isCurrentStep = true,
        continueButtonLoading = false,
      )
    }
  }
}
