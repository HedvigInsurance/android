package com.hedvig.android.navigation.compose

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavGraphBuilder

/**
 * A local which contains the AnimatedVisibilityScope tied to the current navigation's destination.
 * See [NavGraphBuilder.navdestination] for how it's provided.
 */
val LocalNavAnimatedVisibilityScope: ProvidableCompositionLocal<AnimatedVisibilityScope?> = compositionLocalOf {
  null
}
