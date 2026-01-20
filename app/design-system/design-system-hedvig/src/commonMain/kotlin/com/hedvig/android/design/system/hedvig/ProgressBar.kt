package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode.Reverse
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.hedvig.android.design.system.hedvig.tokens.CircularProgressIndicatorTokens
import com.hedvig.android.design.system.hedvig.tokens.LinearProgressIndicatorTokens
import com.hedvig.android.design.system.hedvig.tokens.ThreeDotsProgressIndicatorTokens
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max

@Composable
fun HedvigLinearProgressIndicator(modifier: Modifier = Modifier) {
  val color: Color = LinearProgressIndicatorTokens.ActiveIndicatorColor.value
  val trackColor: Color = LinearProgressIndicatorTokens.TrackColor.value
  val strokeCap: StrokeCap = LinearProgressIndicatorTokens.StrokeCap
  val infiniteTransition = rememberInfiniteTransition()
  // Fractional position of the 'head' and 'tail' of the two lines drawn, i.e. if the head is 0.8
  // and the tail is 0.2, there is a line drawn from between 20% along to 80% along the total
  // width.
  val firstLineHead = infiniteTransition.animateFloat(
    0f,
    1f,
    infiniteRepeatable(
      animation = keyframes {
        durationMillis = LinearProgressIndicatorTokens.AnimationDuration
        0f at LinearProgressIndicatorTokens.FirstLineHeadDelay using LinearProgressIndicatorTokens.FirstLineHeadEasing
        1f at LinearProgressIndicatorTokens.FirstLineHeadDuration + LinearProgressIndicatorTokens.FirstLineHeadDelay
      },
    ),
  )
  val firstLineTail = infiniteTransition.animateFloat(
    0f,
    1f,
    infiniteRepeatable(
      animation = keyframes {
        durationMillis = LinearProgressIndicatorTokens.AnimationDuration
        0f at LinearProgressIndicatorTokens.FirstLineTailDelay using LinearProgressIndicatorTokens.FirstLineTailEasing
        1f at LinearProgressIndicatorTokens.FirstLineTailDuration + LinearProgressIndicatorTokens.FirstLineTailDelay
      },
    ),
  )
  val secondLineHead = infiniteTransition.animateFloat(
    0f,
    1f,
    infiniteRepeatable(
      animation = keyframes {
        durationMillis = LinearProgressIndicatorTokens.AnimationDuration
        0f at LinearProgressIndicatorTokens.SecondLineHeadDelay using LinearProgressIndicatorTokens.SecondLineHeadEasing
        1f at LinearProgressIndicatorTokens.SecondLineHeadDuration + LinearProgressIndicatorTokens.SecondLineHeadDelay
      },
    ),
  )
  val secondLineTail = infiniteTransition.animateFloat(
    0f,
    1f,
    infiniteRepeatable(
      animation = keyframes {
        durationMillis = LinearProgressIndicatorTokens.AnimationDuration
        0f at LinearProgressIndicatorTokens.SecondLineTailDelay using LinearProgressIndicatorTokens.SecondLineTailEasing
        1f at LinearProgressIndicatorTokens.SecondLineTailDuration + LinearProgressIndicatorTokens.SecondLineTailDelay
      },
    ),
  )
  Canvas(
    modifier
      .progressSemantics()
      .size(LinearProgressIndicatorTokens.IndicatorWidth, LinearProgressIndicatorTokens.IndicatorHeight),
  ) {
    val strokeWidth = size.height
    drawLinearIndicatorTrack(trackColor, strokeWidth, strokeCap)
    if (firstLineHead.value - firstLineTail.value > 0) {
      drawLinearIndicator(
        firstLineHead.value,
        firstLineTail.value,
        color,
        strokeWidth,
        strokeCap,
      )
    }
    if (secondLineHead.value - secondLineTail.value > 0) {
      drawLinearIndicator(
        secondLineHead.value,
        secondLineTail.value,
        color,
        strokeWidth,
        strokeCap,
      )
    }
  }
}

@Composable
fun HedvigCircularProgressIndicator(modifier: Modifier = Modifier) {
  val color: Color = CircularProgressIndicatorTokens.ActiveIndicatorColor.value
  val strokeWidth: Dp = CircularProgressIndicatorTokens.ActiveIndicatorWidth
  val trackColor: Color = CircularProgressIndicatorTokens.TrackColor.value
  val strokeCap: StrokeCap = CircularProgressIndicatorTokens.StrokeCap
  val stroke = with(LocalDensity.current) {
    Stroke(width = strokeWidth.toPx(), cap = strokeCap)
  }
  val transition = rememberInfiniteTransition()
  // The current rotation around the circle, so we know where to start the rotation from
  val currentRotation = transition.animateValue(
    0,
    CircularProgressIndicatorTokens.RotationsPerCycle,
    Int.VectorConverter,
    infiniteRepeatable(
      animation = tween(
        durationMillis =
          CircularProgressIndicatorTokens.RotationDuration * CircularProgressIndicatorTokens.RotationsPerCycle,
        easing = CircularProgressIndicatorTokens.LinearEasing,
      ),
    ),
  )
  // How far forward (degrees) the base point should be from the start point
  val baseRotation = transition.animateFloat(
    0f,
    CircularProgressIndicatorTokens.BaseRotationAngle,
    infiniteRepeatable(
      animation = tween(
        durationMillis = CircularProgressIndicatorTokens.RotationDuration,
        easing = CircularProgressIndicatorTokens.LinearEasing,
      ),
    ),
  )
  // How far forward (degrees) both the head and tail should be from the base point
  val endAngle = transition.animateFloat(
    0f,
    CircularProgressIndicatorTokens.JumpRotationAngle,
    infiniteRepeatable(
      animation = keyframes {
        durationMillis =
          CircularProgressIndicatorTokens.HeadAndTailAnimationDuration +
          CircularProgressIndicatorTokens.HeadAndTailDelayDuration
        0f at 0 using CircularProgressIndicatorTokens.CircularEasing
        CircularProgressIndicatorTokens.JumpRotationAngle at
          CircularProgressIndicatorTokens.HeadAndTailAnimationDuration
      },
    ),
  )
  val startAngle = transition.animateFloat(
    0f,
    CircularProgressIndicatorTokens.JumpRotationAngle,
    infiniteRepeatable(
      animation = keyframes {
        durationMillis =
          CircularProgressIndicatorTokens.HeadAndTailAnimationDuration +
          CircularProgressIndicatorTokens.HeadAndTailDelayDuration
        0f at CircularProgressIndicatorTokens.HeadAndTailDelayDuration using
          CircularProgressIndicatorTokens.CircularEasing
        CircularProgressIndicatorTokens.JumpRotationAngle at durationMillis
      },
    ),
  )
  Canvas(
    modifier = modifier
      .progressSemantics()
      .size(CircularProgressIndicatorTokens.IndicatorDiameter),
  ) {
    drawCircularIndicatorTrack(trackColor, stroke)

    val currentRotationAngleOffset =
      (currentRotation.value * CircularProgressIndicatorTokens.RotationAngleOffset) % 360f

    // How long a line to draw using the start angle as a reference point
    val sweep = abs(endAngle.value - startAngle.value)

    // Offset by the constant offset and the per rotation offset
    val offset = CircularProgressIndicatorTokens.StartAngleOffset + currentRotationAngleOffset + baseRotation.value
    drawIndeterminateCircularIndicator(
      startAngle.value + offset,
      strokeWidth,
      sweep,
      color,
      stroke,
    )
  }
}

@Composable
fun HedvigThreeDotsProgressIndicator(modifier: Modifier = Modifier) {
  val density = LocalDensity.current
  val color: Color = ThreeDotsProgressIndicatorTokens.ActiveIndicatorColor.value
  val trackColor: Color = ThreeDotsProgressIndicatorTokens.TrackColor.value
  val indicatorDiameter = with(density) { ThreeDotsProgressIndicatorTokens.IndicatorDiameter.toPx() }
  val spaceBetween = with(density) { ThreeDotsProgressIndicatorTokens.IndicatorSpacing.toPx() }

  val transition = rememberInfiniteTransition()
  val dot1 = transition.animateLoadingDot(StartOffset(0))
  val dot2 = transition.animateLoadingDot(StartOffset(ThreeDotsProgressIndicatorTokens.AnimationDelay))
  val dot3 = transition.animateLoadingDot(StartOffset(ThreeDotsProgressIndicatorTokens.AnimationDelay * 2))
  Canvas(
    modifier = modifier
      .progressSemantics()
      .size(ThreeDotsProgressIndicatorTokens.Size),
  ) {
    drawLoadingDot(color, trackColor, indicatorDiameter, dot1.value, 0f)
    drawLoadingDot(color, trackColor, indicatorDiameter, dot2.value, indicatorDiameter + spaceBetween)
    drawLoadingDot(color, trackColor, indicatorDiameter, dot3.value, indicatorDiameter * 2 + spaceBetween * 2)
  }
}

private fun DrawScope.drawLinearIndicatorTrack(color: Color, strokeWidth: Float, strokeCap: StrokeCap) =
  drawLinearIndicator(0f, 1f, color, strokeWidth, strokeCap)

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

private fun DrawScope.drawCircularIndicatorTrack(color: Color, stroke: Stroke) =
  drawCircularIndicator(0f, 360f, color, stroke)

private fun DrawScope.drawCircularIndicator(startAngle: Float, sweep: Float, color: Color, stroke: Stroke) {
  val diameterOffset = stroke.width / 2
  val arcDimen = size.width - 2 * diameterOffset
  drawArc(
    color = color,
    startAngle = startAngle,
    sweepAngle = sweep,
    useCenter = false,
    topLeft = Offset(diameterOffset, diameterOffset),
    size = Size(arcDimen, arcDimen),
    style = stroke,
  )
}

private fun DrawScope.drawIndeterminateCircularIndicator(
  startAngle: Float,
  strokeWidth: Dp,
  sweep: Float,
  color: Color,
  stroke: Stroke,
) {
  val strokeCapOffset = if (stroke.cap == StrokeCap.Butt) {
    0f
  } else {
    // Length of arc is angle * radius
    // Angle (radians) is length / radius
    // The length should be the same as the stroke width for calculating the min angle
    (180.0 / PI).toFloat() * (strokeWidth / (CircularProgressIndicatorTokens.IndicatorDiameter / 2)) / 2f
  }

  // Adding a stroke cap draws half the stroke width behind the start point, so we want to
  // move it forward by that amount so the arc visually appears in the correct place
  val adjustedStartAngle = startAngle + strokeCapOffset

  // When the start and end angles are in the same place, we still want to draw a small sweep, so
  // the stroke caps get added on both ends and we draw the correct minimum length arc
  val adjustedSweep = max(sweep, 0.1f)

  drawCircularIndicator(adjustedStartAngle, adjustedSweep, color, stroke)
}

@Composable
private fun InfiniteTransition.animateLoadingDot(startOffset: StartOffset): State<Float> = animateValue(
  0f,
  1f,
  Float.VectorConverter,
  infiniteRepeatable(
    animation = tween(
      // Half the duration since we want the `Reverse` animation to be included in this duration
      durationMillis = ThreeDotsProgressIndicatorTokens.AnimationDuration / 2,
      easing = ThreeDotsProgressIndicatorTokens.Easing,
    ),
    repeatMode = Reverse,
    initialStartOffset = startOffset,
  ),
)

private fun DrawScope.drawLoadingDot(color: Color, trackColor: Color, diameter: Float, value: Float, xOffset: Float) {
  val center = Offset(diameter / 2 + xOffset, diameter / 2)
  drawCircle(
    color = trackColor,
    radius = diameter / 2,
    center = center,
  )
  drawCircle(
    color = color,
    radius = (diameter / 2) * value,
    center = center,
  )
}
