package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Creates a sweep gradient on iOS platforms.
 * Note: The startAngle parameter is ignored on non-Android platforms as shader rotation is not available.
 * This fallback implementation uses a standard sweep gradient without rotation.
 */
@Stable
actual fun Brush.Companion.angledSweepGradient(
  vararg colorStops: Pair<Float, Color>,
  center: Offset,
  startAngle: Float,
): Brush = Brush.sweepGradient(
  colorStops = colorStops,
  center = center,
)

/**
 * Creates a sweep gradient on iOS platforms.
 * Note: The startAngle parameter is ignored on non-Android platforms as shader rotation is not available.
 * This fallback implementation uses a standard sweep gradient without rotation.
 */
@Stable
actual fun Brush.Companion.angledSweepGradient(
  colors: List<Color>,
  center: Offset,
  startAngle: Float,
): Brush = Brush.sweepGradient(
  colors = colors,
  center = center,
)
