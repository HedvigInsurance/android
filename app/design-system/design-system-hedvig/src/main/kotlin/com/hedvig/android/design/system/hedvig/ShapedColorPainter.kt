package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter

internal class ShapedColorPainter(
  val shape: Shape,
  val color: Color,
) : Painter() {
  private var alpha: Float = 1.0f

  private var colorFilter: ColorFilter? = null

  override fun DrawScope.onDraw() {
    val outline = shape.createOutline(size, layoutDirection, this)
    when (outline) {
      is Outline.Generic -> {
        drawPath(
          outline.path,
          color = color,
          alpha = alpha,
          colorFilter = colorFilter,
        )
      }
      is Outline.Rectangle -> {
        drawRect(color = color, alpha = alpha, colorFilter = colorFilter)
      }
      is Outline.Rounded -> {
        drawCircle(color = color, alpha = alpha, colorFilter = colorFilter)
      }
    }
  }

  override fun applyAlpha(alpha: Float): Boolean {
    this.alpha = alpha
    return true
  }

  override fun applyColorFilter(colorFilter: ColorFilter?): Boolean {
    this.colorFilter = colorFilter
    return true
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ColorPainter) return false

    if (color != other.color) return false

    return true
  }

  override fun hashCode(): Int {
    return color.hashCode()
  }

  override fun toString(): String {
    return "ColorPainter(color=$color)"
  }

  /**
   * Drawing a color does not have an intrinsic size, return [Size.Unspecified] here
   */
  override val intrinsicSize: Size = Size.Unspecified
}

@Composable
fun rememberShapedColorPainter(
  color: Color,
  shape: Shape = HedvigTheme.shapes.cornerMedium,
): Painter {
  return remember(color, shape) {
    ShapedColorPainter(shape, color)
  }
}
