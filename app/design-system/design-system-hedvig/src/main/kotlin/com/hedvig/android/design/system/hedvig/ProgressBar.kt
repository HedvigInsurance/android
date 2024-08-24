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
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.ProgressDefaults.arcSweepAngle
import com.hedvig.android.design.system.hedvig.ProgressDefaults.circularBaseWidth
import com.hedvig.android.design.system.hedvig.ProgressDefaults.progressAnimationDuration
import com.hedvig.android.design.system.hedvig.ProgressDefaults.stroke
import com.hedvig.android.design.system.hedvig.ProgressDefaults.strokeCap
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.FillPrimary
import com.hedvig.android.design.system.hedvig.tokens.ColorSchemeKeyTokens.SurfaceSecondary
import kotlin.math.abs

@Composable
fun HedvigLinearProgressBar(modifier: Modifier = Modifier) {
  val infiniteTransition = rememberInfiniteTransition(label = "rememberInfiniteTransition")
  val repeatable = infiniteRepeatable<Float>(
    tween(progressAnimationDuration),
    RepeatMode.Restart,
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
    strokeCap = strokeCap,
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
      .height(stroke),
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
  val yOffset = height / 2

  val isLtr = layoutDirection == LayoutDirection.Ltr
  val barStart = (if (isLtr) startFraction else 1f - endFraction) * width
  val barEnd = (if (isLtr) endFraction else 1f - startFraction) * width

  if (strokeCap == StrokeCap.Butt || height > width) {
    drawLine(color, Offset(barStart, yOffset), Offset(barEnd, yOffset), strokeWidth)
  } else {
    val strokeCapOffset = strokeWidth / 2
    val coerceRange = strokeCapOffset..(width - strokeCapOffset)
    val adjustedBarStart = barStart.coerceIn(coerceRange)
    val adjustedBarEnd = barEnd.coerceIn(coerceRange)

    if (abs(endFraction - startFraction) > 0) {
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

private fun DrawScope.drawLinearIndicatorTrack(color: Color, strokeWidth: Float, strokeCap: StrokeCap) =
  drawLinearIndicator(0f, 1f, color, strokeWidth, strokeCap)

@Composable
fun HedvigCircularProgressBar(modifier: Modifier = Modifier) {
  val infiniteTransition = rememberInfiniteTransition(label = "rememberInfiniteTransition")
  val repeatable = infiniteRepeatable<Float>(
    tween(progressAnimationDuration),
    RepeatMode.Restart,
  )
  val animatedArcStart = infiniteTransition.animateFloat(
    -180f,
    180f,
    repeatable,
    label = "animatedProgress",
  )
  Box(
    modifier = modifier,
  ) {
    val arcColor = progressColors.indicatorColor
    val trackColor = progressColors.trackColor
    Canvas(
      modifier = Modifier
        .size(100.dp)
        .clipToBounds(),
    ) {
      drawCircle(
        radius = (circularBaseWidth / 2).toPx(),
        color = trackColor,
        style = Stroke(stroke.toPx(), cap = strokeCap),
      )
      val offsetX = this.center.x - (circularBaseWidth / 2).toPx()
      val offsetY = this.center.y - (circularBaseWidth / 2).toPx()
      drawArc(
        color = arcColor,
        startAngle = animatedArcStart.value,
        sweepAngle = arcSweepAngle,
        topLeft = Offset(offsetX, offsetY),
        useCenter = false,
        size = Size((circularBaseWidth).toPx(), (circularBaseWidth).toPx()),
        style = Stroke(stroke.toPx(), cap = strokeCap),
      )
    }
  }
}

private object ProgressDefaults {
  val strokeCap = StrokeCap.Round
  val progressAnimationDuration = 1800
  val stroke = 4.dp
  val circularBaseWidth = 48.dp
  val arcSweepAngle = 180F
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
        trackColor = fromToken(SurfaceSecondary),
        indicatorColor = fromToken(FillPrimary),
      )
    }
  }
