package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Creates an angled sweep gradient.
 * On Android, this uses a rotated SweepGradient shader for optimal performance.
 * On other platforms, this falls back to a standard sweep gradient without rotation.
 */
@Stable
expect fun Brush.Companion.angledSweepGradient(
  vararg colorStops: Pair<Float, Color>,
  center: Offset = Offset.Unspecified,
  startAngle: Float = 0f,
): Brush

/**
 * Creates an angled sweep gradient.
 * On Android, this uses a rotated SweepGradient shader for optimal performance.
 * On other platforms, this falls back to a standard sweep gradient without rotation.
 */
@Stable
expect fun Brush.Companion.angledSweepGradient(
  colors: List<Color>,
  center: Offset = Offset.Unspecified,
  startAngle: Float = 0f,
): Brush
