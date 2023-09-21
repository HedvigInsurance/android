package com.hedvig.android.core.designsystem.material3.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

private const val ProgressThreshold = 0.35f

private val Int.ForOutgoing: Int
  get() = (this * ProgressThreshold).toInt()

private val Int.ForIncoming: Int
  get() = this - this.ForOutgoing

object FadeThroughDefaults {
  private const val FadeThroughDuration = MotionTokens.DurationMedium1.toInt()

  internal val fadeThroughEnterTransition: EnterTransition = fadeIn(
    animationSpec = tween(
      durationMillis = FadeThroughDuration.ForIncoming,
      delayMillis = FadeThroughDuration.ForOutgoing,
      easing = MotionTokens.EasingStandardCubicBezier,
    ),
  )

  internal val fadeThroughExitTransition: ExitTransition = fadeOut(
    animationSpec = tween(
      durationMillis = FadeThroughDuration.ForOutgoing,
      delayMillis = 0,
      easing = MotionTokens.EasingStandardAccelerateCubicBezier,
    ),
  )
}
