package com.hedvig.android.feature.odyssey.step.audiorecording.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.Surface
import kotlin.math.sqrt

/**
 * @param amplitude A value typically coming from `MediaRecorder.getMaxAmplitude()` which usually ranges from 0-4000,
 *  and can peak to ~20000. Gets transformed to a more reasonable value of 0-1000 for the circle radius.
 */
@Composable
fun RecordingAmplitudeIndicator(amplitude: Int, modifier: Modifier = Modifier) {
  val color = LocalContentColor.current.copy(alpha = 0.12f)
  val animated by animateIntAsState(
    targetValue = (sqrt(amplitude.toDouble()).toInt() * 10).coerceAtMost(1000),
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
