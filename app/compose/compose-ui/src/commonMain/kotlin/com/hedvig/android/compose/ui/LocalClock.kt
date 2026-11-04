package com.hedvig.android.compose.ui

import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.time.Clock

/**
 * The [Clock] composables read the current time from. The app provides the graph's clock at the root, so a test can
 * swap in a fixed one with `CompositionLocalProvider`. Defaults to [Clock.System] so previews need no setup.
 */
val LocalClock = staticCompositionLocalOf<Clock> { Clock.System }
