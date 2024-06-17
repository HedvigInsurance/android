package com.hedvig.android.design.system.hedvig.tokens

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.Easing

internal abstract class TweenAnimationTokens {
  abstract val durationMillis: Int
  abstract val easing: Easing

  data object FastAnimationTokens : TweenAnimationTokens() {
    override val durationMillis: Int = AnimationTokens().fastAnimationDuration
    override val easing: Easing = EaseInOut
  }
}
