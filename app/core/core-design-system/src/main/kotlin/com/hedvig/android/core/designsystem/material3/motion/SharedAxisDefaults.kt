package com.hedvig.android.core.designsystem.material3.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset

private const val ProgressThreshold = 0.3f

private val Int.ForOutgoing: Int
  get() = (this * ProgressThreshold).toInt()

private val Int.ForIncoming: Int
  get() = this - this.ForOutgoing

object SharedAxisDefaults {
  const val SharedAxisOffset = 30.0
  private const val SharedAxisDuration = MotionTokens.DurationMedium2.toInt()

  private val sharedAxisSlideAnimationSpec = tween<IntOffset>(
    durationMillis = SharedAxisDuration,
    delayMillis = 0,
    easing = MotionTokens.EasingStandardCubicBezier,
  )

  internal fun sharedXAxisEnterTransition(initialOffsetX: Int): EnterTransition {
    val slide = slideInHorizontally(
      animationSpec = sharedAxisSlideAnimationSpec,
      initialOffsetX = { initialOffsetX },
    )
    val fade = fadeIn(
      tween(
        durationMillis = SharedAxisDuration.ForIncoming,
        delayMillis = SharedAxisDuration.ForOutgoing,
        easing = MotionTokens.EasingStandardDecelerateCubicBezier,
      ),
    )
    return slide + fade
  }

  internal fun sharedXAxisExitTransition(targetOffsetX: Int): ExitTransition {
    val slide = slideOutHorizontally(
      animationSpec = sharedAxisSlideAnimationSpec,
      targetOffsetX = { targetOffsetX },
    )
    val fade = fadeOut(
      tween(
        durationMillis = SharedAxisDuration.ForOutgoing,
        delayMillis = 0,
        easing = MotionTokens.EasingStandardAccelerateCubicBezier,
      ),
    )
    return slide + fade
  }
}
