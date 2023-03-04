package com.hedvig.android.core.designsystem.material3

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

object MotionDefaults {
  fun sharedXAxisEnter(density: Density): EnterTransition {
    val offsetPixels = with(density) { MotionTokens.SharedAxisOffset.dp.roundToPx() }
    val slide = slideInHorizontally(
      animationSpec = tween(MotionTokens.DurationMedium2.toInt(), 0, MotionTokens.EasingLegacyCubicBezier),
      initialOffsetX = { offsetPixels },
    )
    val fade = fadeIn(
      tween(
        durationMillis = (MotionTokens.DurationMedium2 * 0.7).toInt(),
        delayMillis = (MotionTokens.DurationMedium2 * 0.3).toInt(),
        easing = MotionTokens.EasingLegacyDecelerateCubicBezier,
      ),
    )
    return slide + fade
  }

  fun sharedXAxisExit(density: Density): ExitTransition {
    val offsetPixels = with(density) { MotionTokens.SharedAxisOffset.dp.roundToPx() }
    val slide = slideOutHorizontally(
      animationSpec = tween(MotionTokens.DurationMedium2.toInt(), 0, MotionTokens.EasingLegacyCubicBezier),
      targetOffsetX = { -offsetPixels },
    )
    val fade = fadeOut(
      tween(
        durationMillis = (MotionTokens.DurationMedium2 * 0.3).toInt(),
        delayMillis = 0,
        easing = MotionTokens.EasingLegacyAccelerateCubicBezier,
      ),
    )
    return slide + fade
  }

  fun sharedXAxisPopEnter(density: Density): EnterTransition {
    val offsetPixels = with(density) { MotionTokens.SharedAxisOffset.dp.roundToPx() }
    val slide = slideInHorizontally(
      animationSpec = tween(MotionTokens.DurationMedium2.toInt(), 0, MotionTokens.EasingLegacyCubicBezier),
      initialOffsetX = { -offsetPixels },
    )
    val fade = fadeIn(
      tween(
        durationMillis = (MotionTokens.DurationMedium2 * 0.7).toInt(),
        delayMillis = (MotionTokens.DurationMedium2 * 0.3).toInt(),
        easing = MotionTokens.EasingLegacyDecelerateCubicBezier,
      ),
    )
    return slide + fade
  }

  fun sharedXAxisPopExit(density: Density): ExitTransition {
    val offsetPixels = with(density) { MotionTokens.SharedAxisOffset.dp.roundToPx() }
    val slide = slideOutHorizontally(
      animationSpec = tween(MotionTokens.DurationMedium2.toInt(), 0, MotionTokens.EasingLegacyCubicBezier),
      targetOffsetX = { offsetPixels },
    )
    val fade = fadeOut(
      tween(
        durationMillis = (MotionTokens.DurationMedium2 * 0.3).toInt(),
        delayMillis = 0,
        easing = MotionTokens.EasingLegacyAccelerateCubicBezier,
      ),
    )
    return slide + fade
  }
}
