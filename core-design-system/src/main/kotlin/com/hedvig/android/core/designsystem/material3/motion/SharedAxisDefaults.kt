package com.hedvig.android.core.designsystem.material3.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset

object SharedAxisDefaults {
  const val SharedAxisOffset = 30.0

  private val sharedAxisSlideAnimationSpec = tween<IntOffset>(
    durationMillis = MotionTokens.DurationMedium2.toInt(),
    delayMillis = 0,
    easing = MotionTokens.EasingStandardCubicBezier,
  )

  internal fun sharedXAxisEnterTransition(
    initialOffsetX: Int,
  ): EnterTransition {
    val slide = slideInHorizontally(
      animationSpec = sharedAxisSlideAnimationSpec,
      initialOffsetX = { initialOffsetX },
    )
    val fade = fadeIn(
      tween(
        durationMillis = (MotionTokens.DurationMedium2 * 0.7).toInt(),
        delayMillis = (MotionTokens.DurationMedium2 * 0.3).toInt(),
        easing = MotionTokens.EasingStandardDecelerateCubicBezier,
      ),
    )
    return slide + fade
  }

  internal fun sharedXAxisExitTransition(
    targetOffsetX: Int,
  ): ExitTransition {
    val slide = slideOutHorizontally(
      animationSpec = sharedAxisSlideAnimationSpec,
      targetOffsetX = { targetOffsetX },
    )
    val fade = fadeOut(
      tween(
        durationMillis = (MotionTokens.DurationMedium2 * 0.3).toInt(),
        delayMillis = 0,
        easing = MotionTokens.EasingStandardAccelerateCubicBezier,
      ),
    )
    return slide + fade
  }
}
