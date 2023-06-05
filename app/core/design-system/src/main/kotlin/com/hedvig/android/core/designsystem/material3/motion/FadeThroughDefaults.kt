package com.hedvig.android.core.designsystem.material3.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn

private const val ProgressThreshold = 0.35f

private val Int.ForOutgoing: Int
  get() = (this * ProgressThreshold).toInt()

private val Int.ForIncoming: Int
  get() = this - this.ForOutgoing

object FadeThroughDefaults {
  const val FadeThroughDuration = MotionTokens.DurationMedium2.toInt()
  val FadeThroughInterpolator = MotionTokens.EasingStandardCubicBezier

  internal val fadeThroughEnterTransition: EnterTransition = run {
    val fade = fadeIn(
      tween(
        durationMillis = FadeThroughDuration.ForIncoming,
        delayMillis = FadeThroughDuration.ForOutgoing,
        easing = FadeThroughInterpolator,
      ),
    )
    val scale = scaleIn(
      tween(
        durationMillis = FadeThroughDuration.ForIncoming,
        delayMillis = FadeThroughDuration.ForOutgoing,
        easing = FadeThroughInterpolator,
      ),
      initialScale = 0.92f,
    )
    fade + scale
  }

  internal val fadeThroughExitTransition: ExitTransition = fadeOut(
    animationSpec = tween(
      durationMillis = FadeThroughDuration.ForOutgoing,
      delayMillis = 0,
      easing = MotionTokens.EasingStandardAccelerateCubicBezier,
    ),
  )
}
