package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.tokens.RadioOptionColorTokens
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max

@Composable
fun HedvigLinearProgressBar(
  modifier: Modifier = Modifier,
) {

  val infiniteTransition = rememberInfiniteTransition(label = "rememberInfiniteTransition")
  val repeatable = infiniteRepeatable<Float>(
    tween(2000), RepeatMode.Restart,
  )
  val animatedProgress = infiniteTransition.animateFloat(
    0f,
    1f,
    repeatable,
    label = "animatedProgress",
  )

  LinearProgressIndicator(
      progress = { animatedProgress.value },
      modifier = modifier.fillMaxWidth(),
      color = progressColors.indicatorColor,
      trackColor = progressColors.trackColor,
      strokeCap = ProgressDefaults.strokeCap,
  )
}

@Composable
private fun LinearProgressIndicator(
  progress: () -> Float,
  modifier: Modifier = Modifier,
  color: Color,
  trackColor: Color,
  strokeCap: StrokeCap,
) {
  val coercedProgress = { progress().coerceIn(0f, 1f) }
  Canvas(
      modifier
          .semantics(mergeDescendants = true) {
              progressBarRangeInfo = ProgressBarRangeInfo(coercedProgress(), 0f..1f)
          }
          .height(4.dp), //todo: to tokens
  ) {
    val strokeWidth = size.height
    drawLinearIndicatorTrack(trackColor, strokeWidth, strokeCap)
    drawLinearIndicator(0f, coercedProgress(), color, strokeWidth, strokeCap)
  }
}

private fun DrawScope.drawLinearIndicator(
  startFraction: Float,
  endFraction: Float,
  color: Color,
  strokeWidth: Float,
  strokeCap: StrokeCap,
) {
  val width = size.width
  val height = size.height
  // Start drawing from the vertical center of the stroke
  val yOffset = height / 2

  val isLtr = layoutDirection == LayoutDirection.Ltr
  val barStart = (if (isLtr) startFraction else 1f - endFraction) * width
  val barEnd = (if (isLtr) endFraction else 1f - startFraction) * width

  // if there isn't enough space to draw the stroke caps, fall back to StrokeCap.Butt
  if (strokeCap == StrokeCap.Butt || height > width) {
    // Progress line
    drawLine(color, Offset(barStart, yOffset), Offset(barEnd, yOffset), strokeWidth)
  } else {
    // need to adjust barStart and barEnd for the stroke caps
    val strokeCapOffset = strokeWidth / 2
    val coerceRange = strokeCapOffset..(width - strokeCapOffset)
    val adjustedBarStart = barStart.coerceIn(coerceRange)
    val adjustedBarEnd = barEnd.coerceIn(coerceRange)

    if (abs(endFraction - startFraction) > 0) {
      // Progress line
      drawLine(
        color,
        Offset(adjustedBarStart, yOffset),
        Offset(adjustedBarEnd, yOffset),
        strokeWidth,
        strokeCap,
      )
    }
  }
}

private fun DrawScope.drawLinearIndicatorTrack(
  color: Color,
  strokeWidth: Float,
  strokeCap: StrokeCap,
) = drawLinearIndicator(0f, 1f, color, strokeWidth, strokeCap)

@Composable
fun CircularProgressBar(
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "rememberInfiniteTransition")
  val repeatable =   infiniteRepeatable<Float>(
    tween(2000), RepeatMode.Restart,
  )
  val animatedArcStart = infiniteTransition.animateFloat(
    -90f,
    270f,
    repeatable,
    label = "animatedProgress",
  )
  val animatedArcEnd = infiniteTransition.animateFloat(
    0f,
    360f,
    repeatable,
    label = "animatedProgress",
  )

  Box(
    modifier = modifier
  ) {
    val stroke = 4.dp
    val cap = StrokeCap.Round
    val width = 96.dp
    Canvas(
      modifier = Modifier
        .size(100.dp)
        .clipToBounds()
    ) {
      drawCircle(
        radius = (width /4).toPx(),
        color = Color.LightGray,
        style = Stroke(stroke.toPx(), cap = cap)
      )
      val offsetX = this.center.x - (width/4).toPx()
      val offsetY = this.center.y - (width/4).toPx()

      drawArc(
        color = Color.Black,
        startAngle = animatedArcStart.value,
        sweepAngle = animatedArcEnd.value,
        topLeft = Offset(offsetX,offsetY),
        useCenter = false,
        size = Size((width/2).toPx(),(width/2).toPx()),
        style = Stroke(stroke.toPx(), cap = cap)
      )
    }
  }
}


private object ProgressDefaults{
  val strokeCap = StrokeCap.Round
  val circularIndicatorDiameter = 48f
}


private data class ProgressColors(
  val trackColor: Color,
  val indicatorColor: Color,
)

private val progressColors: ProgressColors
  @Composable
  get() = with(HedvigTheme.colorScheme) {
    remember(this) {
      ProgressColors(
          trackColor = fromToken(RadioOptionColorTokens.ContainerColor),
          indicatorColor = fromToken(RadioOptionColorTokens.OptionTextColor),
      )
    }
  }
