package com.hedvig.feature.claim.chat.ui.audiorecording

import android.view.View
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.audio.player.HedvigAudioPlayer
import com.hedvig.android.audio.player.audioplayer.rememberAudioPlayer
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCircularProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.tokens.MotionTokens
import com.hedvig.audio.player.data.AudioPlayer
import com.hedvig.audio.player.data.AudioPlayerState
import com.hedvig.audio.player.data.PlayableAudioSource
import com.hedvig.audio.player.data.ProgressPercentage
import com.hedvig.audio.player.data.SignedAudioUrl
import hedvig.resources.R
import java.io.File
import java.text.DecimalFormat
import kotlin.math.sqrt
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun AudioRecorder(
  uiState: AudioRecordingStepState.AudioRecording,
  startRecording: () -> Unit,
  clock: Clock,
  stopRecording: () -> Unit,
  submitAudioFile: (File) -> Unit,
  submitAudioUrl: (AudioUrl) -> Unit,
  redo: () -> Unit,
  allowFreeText: Boolean,
  onLaunchFreeText: () -> Unit,
  isCurrentStep: Boolean,
  canSkip: Boolean,
  onSkip: () -> Unit,
  modifier: Modifier = Modifier,
) {
  when (uiState) {
    is AudioRecordingStepState.AudioRecording.Playback -> Playback(
      uiState = uiState,
      submit = {
        val filePath = uiState.filePath
        val audioFile = File(filePath)
        submitAudioFile(audioFile)
      },
      redo = redo,
      modifier = modifier,
    )

    is AudioRecordingStepState.AudioRecording.PrerecordedWithAudioContent ->
      PrerecordedPlayback(
      uiState = uiState,
      submitAudioUrl = {
        submitAudioUrl(uiState.audioContent.audioUrl)
      },
      redo = redo,
      isCurrentStep = isCurrentStep,
      modifier = modifier,
      canSkip = canSkip,
      onSkip = onSkip
    )

    else -> {
      val isRecording = uiState is AudioRecordingStepState.AudioRecording.Recording
      val isRecordingTransition = updateTransition(isRecording)
      if (isRecording) {
        ScreenOnFlag()
      }

      val startRecordingText = stringResource(R.string.EMBARK_START_RECORDING)
      val stopRecordingText = stringResource(R.string.EMBARK_STOP_RECORDING)
      val audioRecordingText = stringResource(R.string.A11Y_AUDIO_RECORDING)
      val recordingState = stringResource(R.string.TALKBACK_RECORDING_NOW)
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
      ) {
        Spacer(Modifier.height(80.dp))
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
              Modifier
                .size(size)
                .background(color, RoundedCornerShape(cornerRadius)),
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
        val twoDigitsFormat = remember { DecimalFormat("00") }
        isRecordingTransition.AnimatedContent(
          transitionSpec = {
            val animationSpec = tween<IntOffset>(MotionTokens.DurationLong1.toInt())
            val animationSpecFade = tween<Float>(MotionTokens.DurationMedium1.toInt())
            val animationSpecFloat = tween<Float>(MotionTokens.DurationLong1.toInt())
            val scale = 0.6f
            val enterTransition =
              slideInVertically(animationSpec) +
                fadeIn(animationSpecFade) +
                scaleIn(animationSpecFloat, initialScale = scale)
            val exitTransition =
              slideOutVertically(animationSpec) +
                fadeOut(animationSpecFade) +
                scaleOut(animationSpecFloat, targetScale = scale)
            enterTransition togetherWith exitTransition
          },
          contentAlignment = Alignment.Center,
          modifier = Modifier.fillMaxWidth(),
        ) { isRecording ->
          if (isRecording) {
            val diff = clock.now() - (startedRecordingAt ?: clock.now())
            val label =
              "${twoDigitsFormat.format(diff.inWholeMinutes)}:${twoDigitsFormat.format(diff.inWholeSeconds % 60)}"
            val durationDescription = stringResource(R.string.TALKBACK_RECORDING_DURATION, label)
            HedvigText(
              text = label,
              style = HedvigTheme.typography.bodySmall,
              textAlign = TextAlign.Center,
              modifier = Modifier
                .padding(bottom = 16.dp)
                .clearAndSetSemantics {
                  contentDescription = durationDescription
                },
            )
          } else {
            if (allowFreeText) {
              HedvigTextButton(
                text = stringResource(R.string.CLAIMS_USE_TEXT_INSTEAD),
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

@Composable
private fun Playback(uiState: AudioRecordingStepState.AudioRecording.Playback, submit: () -> Unit, redo: () -> Unit, modifier: Modifier = Modifier) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
  ) {
    if (!uiState.isPrepared) {
      HedvigCircularProgressIndicator()
    } else {
      val audioPlayer = rememberAudioPlayer(PlayableAudioSource.LocalFilePath(uiState.filePath))
      HedvigAudioPlayer(audioPlayer = audioPlayer)
    }

    HedvigButton(
      onClick = submit,
      text = stringResource(R.string.SAVE_AND_CONTINUE_BUTTON_LABEL),
      isLoading = uiState.isLoading,
      enabled = uiState.canSubmit,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp),
    )

    HedvigTextButton(
      text = stringResource(R.string.EMBARK_RECORD_AGAIN),
      onClick = redo,
      enabled = uiState.canSubmit,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp),
    )
  }
}

@Composable
private fun PrerecordedPlayback(
  uiState: AudioRecordingStepState.AudioRecording.PrerecordedWithAudioContent,
  redo: () -> Unit,
  submitAudioUrl: () -> Unit,
  isCurrentStep: Boolean,
  canSkip: Boolean,
  onSkip: () -> Unit,
  modifier: Modifier = Modifier,
  audioPlayer: AudioPlayer = rememberAudioPlayer(
    PlayableAudioSource.RemoteUrl(SignedAudioUrl.fromSignedAudioUrlString(uiState.audioContent.signedUrl.value)),
  ),
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth(),
  ) {
    val audioPlayerState = audioPlayer.audioPlayerState.collectAsStateWithLifecycle().value
    HedvigAudioPlayer(audioPlayer = audioPlayer)
    if (isCurrentStep) {
      AnimatedVisibility(
        visible = audioPlayerState is AudioPlayerState.Ready,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically, clip = false),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically, clip = false),
      ) {
        Column {
          Spacer(Modifier.height(16.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (canSkip) {
              HedvigTextButton(
                stringResource(R.string.claims_skip_button),
                onClick = onSkip,
                buttonSize = ButtonDefaults.ButtonSize.Medium,
              )
              Spacer(Modifier.width(16.dp))
            }
            HedvigButton(
              onClick = submitAudioUrl,
              text = stringResource(R.string.CHAT_UPLOAD_PRESS_SEND_LABEL),
              isLoading = uiState.isLoading,
              enabled = true,
            )
          }
        }
      }

      AnimatedVisibility(
        visible = audioPlayerState is AudioPlayerState.Failed || audioPlayerState is AudioPlayerState.Ready,
        enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically, clip = false),
        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically, clip = false),
      ) {
        Column {
          Spacer(Modifier.height(8.dp))
          HedvigTextButton(
            text = stringResource(R.string.EMBARK_RECORD_AGAIN),
            onClick = redo,
            enabled = true,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
  }
}

/**
 * @param amplitude A value typically coming from `MediaRecorder.getMaxAmplitude()` which usually ranges from 0-4000,
 *  and can peak to ~20000. Gets transformed to a more reasonable value of 0-1000 for the circle radius.
 */
@Composable
fun RecordingAmplitudeIndicator(amplitude: Int, modifier: Modifier = Modifier) {
  val color = LocalContentColor.current.copy(alpha = 0.12f)
  val animated by animateIntAsState(
    targetValue = (sqrt(amplitude.toDouble()).toInt() * 10)
      .coerceAtMost(300),
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

/**
 * While this composable is in composition the phone screen stays awake. This is automatically cleared when the
 * composable leaves the composition.
 * Keeps an internal ref count scoped to the current [View] to make sure that multiple calls to this composable don't
 * negate other callers.
 */
@Composable
internal fun ScreenOnFlag() {
  val view = LocalView.current
  DisposableEffect(view) {
    val keepScreenOnState = view.keepScreenOnState
    keepScreenOnState.request()
    onDispose {
      keepScreenOnState.release()
    }
  }
}

private val View.keepScreenOnState: KeepScreenOnState
  get() = getTag(R.id.keep_screen_on_state) as? KeepScreenOnState
    ?: KeepScreenOnState(this).also { setTag(R.id.keep_screen_on_state, it) }

private class KeepScreenOnState(private val view: View) {
  private var refCount = 0
    set(value) {
      val newValue = value.coerceAtLeast(0)
      field = newValue
      view.keepScreenOn = newValue > 0
    }

  fun request() {
    refCount++
  }

  fun release() {
    refCount--
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
        submitAudioUrl = {},
        redo = {},
        allowFreeText = true,
        onLaunchFreeText = {},
        isCurrentStep = true,
        onSkip = {},
        canSkip = true
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
        uiState = AudioRecordingStepState.AudioRecording.Recording(listOf(70),
          Clock.System.now().minus(1019.seconds), ""),
        startRecording = { },
        clock = Clock.System,
        stopRecording = { },
        submitAudioFile = {},
        submitAudioUrl = {},
        allowFreeText = false,
        onLaunchFreeText = {},
        redo = { },
        isCurrentStep = true,
        onSkip = {},
        canSkip = true
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewPrerecordedPlayback() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PrerecordedPlayback(
        uiState = AudioRecordingStepState.AudioRecording.PrerecordedWithAudioContent(AudioContent(
          AudioUrl(""),
          AudioUrl(""),
        )),
        redo = {},
        submitAudioUrl = {},
        modifier = Modifier,
        isCurrentStep = true,
        onSkip = {},
        canSkip = true,
        audioPlayer = object : AudioPlayer {
          override val audioPlayerState: StateFlow<AudioPlayerState> = MutableStateFlow(
            AudioPlayerState.Ready.done(),
          )

          override fun initialize() = error("Not implemented")

          override fun startPlayer() = error("Not implemented")

          override fun pausePlayer() = error("Not implemented")

          override fun retryLoadingAudio() = error("Not implemented")

          override fun seekTo(progressPercentage: ProgressPercentage) = error("Not implemented")

          override fun close() = error("Not implemented")
        },
      )
    }
  }
}
