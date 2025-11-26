
package com.hedvig.android.design.system.hedvig

import android.graphics.SweepGradient
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.util.fastForEachIndexed
import androidx.core.graphics.transform

@Suppress("unused")
@Stable
actual fun Brush.Companion.angledSweepGradient(
  vararg colorStops: Pair<Float, Color>,
  center: Offset,
  startAngle: Float,
): Brush = AngledSweepGradient(
  colors = List(colorStops.size) { i -> colorStops[i].second },
  stops = List(colorStops.size) { i -> colorStops[i].first },
  center = center,
  startAngle = startAngle,
)

@Stable
actual fun Brush.Companion.angledSweepGradient(
  colors: List<Color>,
  center: Offset,
  startAngle: Float,
): Brush = AngledSweepGradient(
  colors = colors,
  stops = null,
  center = center,
  startAngle = startAngle,
)

@Immutable
class AngledSweepGradient internal constructor(
  private val center: Offset,
  private val colors: List<Color>,
  private val stops: List<Float>? = null,
  private val startAngle: Float,
) : ShaderBrush() {
  override fun createShader(size: Size): Shader = AngledSweepGradientShader(
    if (center.isUnspecified) {
      size.center
    } else {
      Offset(
        if (center.x == Float.POSITIVE_INFINITY) size.width else center.x,
        if (center.y == Float.POSITIVE_INFINITY) size.height else center.y,
      )
    },
    colors,
    stops,
    startAngle,
  )

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AngledSweepGradient) return false

    if (center != other.center) return false
    if (colors != other.colors) return false
    if (stops != other.stops) return false
    if (startAngle != other.startAngle) return false

    return true
  }

  override fun hashCode(): Int {
    var result = center.hashCode()
    result = 31 * result + colors.hashCode()
    result = 31 * result + (stops?.hashCode() ?: 0)
    result = 31 * result + startAngle.hashCode()
    return result
  }

  override fun toString(): String {
    val centerValue = if (center.isSpecified) "center=$center, " else ""
    return "AngledSweepGradient(" +
      centerValue +
      "colors=$colors, stops=$stops, startAngle=$startAngle)"
  }
}

@Suppress("FunctionName")
internal fun AngledSweepGradientShader(
  center: Offset,
  colors: List<Color>,
  colorStops: List<Float>?,
  startAngle: Float,
): Shader {
  validateColorStops(colors, colorStops)
  val numTransparentColors = countTransparentColors(colors)
  val shader = SweepGradient(
    center.x,
    center.y,
    makeTransparentColors(colors, numTransparentColors),
    makeTransparentStops(colorStops, colors, numTransparentColors),
  )
  shader.transform { setRotate(startAngle, center.x, center.y) }
  return shader
}

private fun validateColorStops(colors: List<Color>, colorStops: List<Float>?) {
  if (colorStops == null) {
    if (colors.size < 2) {
      throw IllegalArgumentException(
        "colors must have length of at least 2 if colorStops " +
          "is omitted.",
      )
    }
  } else if (colors.size != colorStops.size) {
    throw IllegalArgumentException(
      "colors and colorStops arguments must have" +
        " equal length.",
    )
  }
}

@VisibleForTesting
internal fun countTransparentColors(colors: List<Color>): Int {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    return 0
  }
  var numTransparentColors = 0
  // Don't count the first and last value because we don't add stops for those
  for (i in 1 until colors.lastIndex) {
    if (colors[i].alpha == 0f) {
      numTransparentColors++
    }
  }
  return numTransparentColors
}

@VisibleForTesting
internal fun makeTransparentColors(colors: List<Color>, numTransparentColors: Int): IntArray {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    // No change for Android O+, map the colors directly to their argb equivalent
    return IntArray(colors.size) { i -> colors[i].toArgb() }
  }
  val values = IntArray(colors.size + numTransparentColors)
  var valuesIndex = 0
  val lastIndex = colors.lastIndex
  colors.fastForEachIndexed { index, color ->
    if (color.alpha == 0f) {
      when (index) {
        0 -> {
          values[valuesIndex++] = colors[1].copy(alpha = 0f).toArgb()
        }
        lastIndex -> {
          values[valuesIndex++] = colors[index - 1].copy(alpha = 0f).toArgb()
        }
        else -> {
          val previousColor = colors[index - 1]
          values[valuesIndex++] = previousColor.copy(alpha = 0f).toArgb()

          val nextColor = colors[index + 1]
          values[valuesIndex++] = nextColor.copy(alpha = 0f).toArgb()
        }
      }
    } else {
      values[valuesIndex++] = color.toArgb()
    }
  }
  return values
}

internal fun makeTransparentStops(stops: List<Float>?, colors: List<Color>, numTransparentColors: Int): FloatArray? {
  if (numTransparentColors == 0) {
    return stops?.toFloatArray()
  }
  val newStops = FloatArray(colors.size + numTransparentColors)
  newStops[0] = stops?.get(0) ?: 0f
  var newStopsIndex = 1
  for (i in 1 until colors.lastIndex) {
    val color = colors[i]
    val stop = stops?.get(i) ?: (i.toFloat() / colors.lastIndex)
    newStops[newStopsIndex++] = stop
    if (color.alpha == 0f) {
      newStops[newStopsIndex++] = stop
    }
  }
  newStops[newStopsIndex] = stops?.get(colors.lastIndex) ?: 1f
  return newStops
}
