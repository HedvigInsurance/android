package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.animation.core.CubicBezierEasing

@Suppress("unused")
object MotionTokens {
  const val DurationExtraLong2 = 800.0
  const val DurationExtraLong3 = 900.0
  const val DurationExtraLong4 = 1000.0
  const val DurationLong1 = 450.0
  const val DurationLong2 = 500.0
  const val DurationLong3 = 550.0
  const val DurationLong4 = 600.0
  const val DurationMedium1 = 250.0
  const val DurationMedium2 = 300.0
  const val DurationMedium3 = 350.0
  const val DurationMedium4 = 400.0
  const val DurationShort1 = 50.0
  const val DurationShort2 = 100.0
  const val DurationShort3 = 150.0
  const val DurationShort4 = 200.0
  val EasingEmphasizedCubicBezier = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
  val EasingEmphasizedAccelerateCubicBezier = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
  val EasingEmphasizedDecelerateCubicBezier = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
  val EasingLegacyCubicBezier = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
  val EasingLegacyAccelerateCubicBezier = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
  val EasingLegacyDecelerateCubicBezier = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
  val EasingLinearCubicBezier = CubicBezierEasing(0.0f, 0.0f, 1.0f, 1.0f)
  val EasingStandardCubicBezier = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
  val EasingStandardAccelerateCubicBezier = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)
  val EasingStandardDecelerateCubicBezier = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)
}
