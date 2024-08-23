package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.asFloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.hedvig.android.design.system.hedvig.tokens.AnimationTokens
import com.hedvig.android.design.system.hedvig.tokens.RadioOptionColorTokens
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlinx.coroutines.delay

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
  var iteration by remember { mutableStateOf(0) }
  val infiniteTransition = rememberInfiniteTransition(label = "rememberInfiniteTransition")
  val repeatable =   infiniteRepeatable<Float>(
    tween(2000), RepeatMode.Restart,
  )
  val animatedProgress = infiniteTransition.animateFloat(
    0f,
    1f,
    repeatable,
    label = "animatedProgress",
  )
  LaunchedEffect(Unit) {
    while(true) {
      delay(2000)
      iteration+=1
    }
  }
  val progressLambda = { animatedProgress.value }
  CircularProgressIndicator(
    progress = progressLambda,
    modifier = modifier,
    color = progressColors.circularIndicatorColor(iteration%2!=0),
    strokeWidth = 4.dp, //todo: to defaults
    trackColor = progressColors.circularTrackColor(iteration%2!=0),
    strokeCap = ProgressDefaults.strokeCap,
  )
}

@Composable
private fun CircularProgressIndicator(
  progress: () -> Float,
  modifier: Modifier = Modifier,
  color: Color,
  strokeWidth: Dp,
  trackColor: Color,
  strokeCap: StrokeCap,
) {
  val coercedProgress = { progress().coerceIn(0f, 1f) }
  val stroke = with(LocalDensity.current) {
    Stroke(width = strokeWidth.toPx(), cap = strokeCap)
  }
  Canvas(
    modifier
      .semantics(mergeDescendants = true) {
        progressBarRangeInfo = ProgressBarRangeInfo(coercedProgress(), 0f..1f)
      }
      .size(ProgressDefaults.circularIndicatorDiameter.dp)
  ) {
    // Start at 12 o'clock
    val startAngle = 270f
    val sweep = coercedProgress() * 360f
    drawCircularIndicatorTrack(trackColor, stroke)
    drawDeterminateCircularIndicator(startAngle = startAngle, sweep = sweep, color = color, stroke = stroke)
  }
}

private fun DrawScope.drawCircularIndicatorTrack(
  color: Color,
  stroke: Stroke
) = drawCircularIndicator(0f, 360f, color, stroke)

private fun DrawScope.drawDeterminateCircularIndicator(
  startAngle: Float,
  sweep: Float,
  color: Color,
  stroke: Stroke
) = drawCircularIndicator(startAngle, sweep, color, stroke)

private fun DrawScope.drawIndeterminateCircularIndicator(
  startAngle: Float,
  strokeWidth: Dp,
  sweep: Float,
  color: Color,
  stroke: Stroke
) {
  val strokeCapOffset = if (stroke.cap == StrokeCap.Butt) {
    0f
  } else {
    // Length of arc is angle * radius
    // Angle (radians) is length / radius
    // The length should be the same as the stroke width for calculating the min angle
    (180.0 / PI).toFloat() * (strokeWidth / (ProgressDefaults.circularIndicatorDiameter.dp / 2)) / 2f
  }

  // Adding a stroke cap draws half the stroke width behind the start point, so we want to
  // move it forward by that amount so the arc visually appears in the correct place
  val adjustedStartAngle = startAngle + strokeCapOffset

  // When the start and end angles are in the same place, we still want to draw a small sweep, so
  // the stroke caps get added on both ends and we draw the correct minimum length arc
  val adjustedSweep = max(sweep, 0.1f)

  drawCircularIndicator(adjustedStartAngle, adjustedSweep, color, stroke)
}

private fun DrawScope.drawCircularIndicator(
  startAngle: Float,
  sweep: Float,
  color: Color,
  stroke: Stroke
) {
  // To draw this circle we need a rect with edges that line up with the midpoint of the stroke.
  // To do this we need to remove half the stroke width from the total diameter for both sides.
  val diameterOffset = stroke.width / 2
  val arcDimen = size.width - 2 * diameterOffset
  drawArc(
    color = color,
    startAngle = startAngle,
    sweepAngle = sweep,
    useCenter = false,
    topLeft = Offset(diameterOffset, diameterOffset),
    size = Size(arcDimen, arcDimen),
    style = stroke
  )
}

private object ProgressDefaults{
  val strokeCap = StrokeCap.Round
  val circularIndicatorDiameter = 48f
}


private data class ProgressColors(
  val trackColor: Color,
  val indicatorColor: Color,
) {
  @Composable
  fun circularTrackColor(isReverse: Boolean): Color {
    val targetValue = when {
      isReverse -> indicatorColor
      else -> trackColor
    }
    return targetValue
  }
  @Composable
  fun circularIndicatorColor(isReverse: Boolean): Color {
    val targetValue = when {
      isReverse -> trackColor
      else -> indicatorColor
    }
    return targetValue
  }
}


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
