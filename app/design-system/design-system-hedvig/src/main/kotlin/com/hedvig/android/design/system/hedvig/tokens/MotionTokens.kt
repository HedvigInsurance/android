package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.animation.core.CubicBezierEasing

internal object MotionTokens {
  const val DurationMedium1 = 250.0
  val EasingStandardCubicBezier = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
  val EasingStandardAccelerateCubicBezier = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)
}
