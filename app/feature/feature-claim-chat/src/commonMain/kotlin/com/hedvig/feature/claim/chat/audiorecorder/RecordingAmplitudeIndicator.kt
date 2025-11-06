package com.hedvig.feature.claim.chat.audiorecorder

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import kotlin.math.sqrt

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
