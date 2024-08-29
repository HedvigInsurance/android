package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

internal object CircularProgressIndicatorTokens {
  val ActiveIndicatorColor = ColorSchemeKeyTokens.FillPrimary
  val ActiveIndicatorWidth = 4.0.dp
  val TrackColor = ColorSchemeKeyTokens.SurfaceSecondaryTransparent
  val StrokeCap: StrokeCap = androidx.compose.ui.graphics.StrokeCap.Square
  val Size = 48.0.dp
  val IndicatorDiameter = Size - ActiveIndicatorWidth * 2
  const val RotationDuration = 1332
  const val RotationsPerCycle = 5
  const val BaseRotationAngle = 286f
  const val JumpRotationAngle = 290f
  const val StartAngleOffset = -90f
  val RotationAngleOffset = (BaseRotationAngle + JumpRotationAngle) % 360f
  val HeadAndTailAnimationDuration = (RotationDuration * 0.5).toInt()
  val HeadAndTailDelayDuration = HeadAndTailAnimationDuration
  val LinearEasing = androidx.compose.animation.core.LinearEasing
  val CircularEasing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
}

internal object LinearProgressIndicatorTokens {
  val ActiveIndicatorColor = ColorSchemeKeyTokens.FillPrimary
  val TrackColor = ColorSchemeKeyTokens.SurfaceSecondary
  val StrokeCap: StrokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
  val IndicatorWidth = 240.dp
  val IndicatorHeight = 4.0.dp
  const val AnimationDuration = 1800
  const val FirstLineHeadDuration = 750
  const val FirstLineTailDuration = 850
  const val SecondLineHeadDuration = 567
  const val SecondLineTailDuration = 533

  // Delay before the start of the head and tail animations for both lines
  const val FirstLineHeadDelay = 0
  const val FirstLineTailDelay = 333
  const val SecondLineHeadDelay = 1000
  const val SecondLineTailDelay = 1267

  val FirstLineHeadEasing = CubicBezierEasing(0.2f, 0f, 0.8f, 1f)
  val FirstLineTailEasing = CubicBezierEasing(0.4f, 0f, 1f, 1f)
  val SecondLineHeadEasing = CubicBezierEasing(0f, 0f, 0.65f, 1f)
  val SecondLineTailEasing = CubicBezierEasing(0.1f, 0f, 0.45f, 1f)
}

internal object ThreeDotsProgressIndicatorTokens {
  val ActiveIndicatorColor = ColorSchemeKeyTokens.FillPrimary
  val TrackColor = ColorSchemeKeyTokens.SurfaceSecondary
  val IndicatorDiameter = 6.dp
  val IndicatorSpacing = 6.dp
  val Size = DpSize(IndicatorDiameter * 3 + IndicatorSpacing * 2, IndicatorDiameter)
  val AnimationDuration = 1500
  val AnimationDelay = 333
  val Easing = CubicBezierEasing(0.2f, 0.2f, 0.4f, 0.8f)
}
