package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import com.hedvig.android.design.system.hedvig.tokens.CircularProgressIndicatorTokens.LinearEasing

fun Modifier.animatedBorder(width: Dp, colors: List<Color>, shape: Shape): Modifier {
  return composed {
    val infiniteTransition = rememberInfiniteTransition(label = "InfiniteColorAnimation")
    val progress by infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      label = "border rotation progress",
      animationSpec = infiniteRepeatable(
        animation = tween(
          durationMillis = 8000,
          easing = LinearEasing,
        ),
        repeatMode = RepeatMode.Restart,
      ),
    )
    val brush = Brush.angledSweepGradient(
      colors = colors,
      startAngle = progress,
    )
    drawWithContent {
      drawContent()
      drawOutline(
        outline = shape.createOutline(size, layoutDirection, this),
        brush = brush,
        style = Stroke(width.toPx()),
      )
    }
  }
}
