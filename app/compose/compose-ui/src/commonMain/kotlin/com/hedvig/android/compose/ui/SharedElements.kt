package com.hedvig.android.compose.ui

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * A local which contains the SharedTransitionScope wrapping the entire app. This is always taking up the entire
 * screen's size and must be provided by the app's main activity
 */
val LocalSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope?> = staticCompositionLocalOf {
  null
}
