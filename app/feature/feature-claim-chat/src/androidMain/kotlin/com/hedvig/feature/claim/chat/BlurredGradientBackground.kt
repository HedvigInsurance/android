package com.hedvig.feature.claim.chat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BlurredGradientBackground(modifier: Modifier = Modifier) {
  // 1. Setup Continuous Animation for Movement
  val infiniteTransition = rememberInfiniteTransition(label = "GradientMovement")

  // Animate a phase shift from 0f to 2 * PI (one full cycle) over 10 seconds (10000ms)
  val phase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = (2 * kotlin.math.PI).toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 10000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "phaseShift"
  )

  Box(modifier = modifier.fillMaxSize()) {

    // 2. The Blur and Canvas Layer (Combined for Skia/Android)
    Canvas(
      modifier = Modifier
        .fillMaxSize()
        // IMPORTANT: Apply graphicsLayer to the Canvas itself.
        .graphicsLayer {
          // Force a new layer boundary; sometimes required for blur to work
          alpha = 0.99f
          renderEffect = BlurEffect(
            radiusX = 100.dp.toPx(), // Significant blur radius
            radiusY = 100.dp.toPx(),
            edgeTreatment = TileMode.Decal
          )
        }
    ) {
      val canvasWidth = size.width
      val canvasHeight = size.height

      // Set radius extremely large so only the blurred, soft edges are visible
      val radius = size.minDimension * 2.0f

      // --- Shape 1: Purple/Pink (Top Left Corner) ---
      val offset1 = Offset(
        // Base position + oscillating movement
        x = canvasWidth * 0.1f + sin(phase) * (canvasWidth * 0.15f),
        y = canvasHeight * 0.1f + cos(phase * 0.5f) * (canvasHeight * 0.1f)
      )
      drawCircle(
        color = Color(0xFFC9A7ED).copy(alpha = 0.6f), // Light purple color
        radius = radius,
        center = offset1,
        // BlendMode.Screen helps the colors brighten when they overlap, matching the video's look
        blendMode = BlendMode.Screen
      )

      // --- Shape 2: Yellow/Green (Bottom Right Corner) ---
      val offset2 = Offset(
        x = canvasWidth * 0.9f + cos(phase * 1.5f) * (canvasWidth * 0.1f),
        y = canvasHeight * 0.9f + sin(phase * 0.7f) * (canvasHeight * 0.15f)
      )
      drawCircle(
        color = Color(0xFFF9E899).copy(alpha = 0.5f), // Light yellow/cream color
        radius = radius * 0.8f,
        center = offset2,
        blendMode = BlendMode.Screen
      )
    }
  }
}
