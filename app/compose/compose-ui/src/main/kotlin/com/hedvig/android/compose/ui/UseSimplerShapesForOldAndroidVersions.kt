package com.hedvig.android.compose.ui

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * https://issuetracker.google.com/issues/347031392
 * In API versions 27 and lower, we default to a rounded corner shape instead of the more intricate shape since there
 * is a bug with nothing rendering at all for all UI clipped with any generic shape.
 */
val UseSimplerShapesForOldAndroidVersions = staticCompositionLocalOf { false }
