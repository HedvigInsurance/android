package com.hedvig.android.navigation.compose

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * A local which contains the AnimatedVisibilityScope tied to the current navigation's destination.
 */
val LocalNavAnimatedVisibilityScope: ProvidableCompositionLocal<AnimatedVisibilityScope?> = compositionLocalOf {
  null
}
