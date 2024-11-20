package com.hedvig.android.design.system.hedvig.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.hedvig.android.design.system.hedvig.tokens.MotionTokens

internal object FadeThroughDefaults {
  private const val FadeThroughDuration = MotionTokens.DurationMedium1.toInt()

  val fadeThroughEnterTransition: EnterTransition = fadeIn(
    animationSpec = tween(
      durationMillis = FadeThroughDuration.ForIncoming,
      delayMillis = FadeThroughDuration.ForOutgoing,
      easing = MotionTokens.EasingStandardCubicBezier,
    ),
  )

  val fadeThroughExitTransition: ExitTransition = fadeOut(
    animationSpec = tween(
      durationMillis = FadeThroughDuration.ForOutgoing,
      delayMillis = 0,
      easing = MotionTokens.EasingStandardAccelerateCubicBezier,
    ),
  )
}

private const val ProgressThreshold = 0.35f

private val Int.ForOutgoing: Int
  get() = (this * ProgressThreshold).toInt()

private val Int.ForIncoming: Int
  get() = this - this.ForOutgoing
