package com.hedvig.android.design.system.hedvig.tokens

import androidx.annotation.FloatRange
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath

internal object ShapeTokens {
  val CornerXXLarge: Shape = FigmaShape(24.dp)
  val CornerXLarge: Shape = FigmaShape(16.dp)
  val CornerLarge: Shape = FigmaShape(12.dp)
  val CornerMedium: Shape = FigmaShape(10.dp)
  val CornerSmall: Shape = FigmaShape(8.dp)
  val CornerExtraSmall: Shape = FigmaShape(4.dp)
  val CornerNone: Shape = RectangleShape
}

private fun RoundedPolygon.Companion.squircle(
  width: Float,
  height: Float,
  cornerRadius: Float,
  @FloatRange(from = 0.0, to = 1.0) smoothing: Float,
): Path {
  if (width == 0f || height == 0f) {
    return Path()
  }
  @Suppress("ktlint:standard:argument-list-wrapping")
  return RoundedPolygon(
    vertices = floatArrayOf(
      0f, 0f,
      width, 0f,
      width, height,
      0f, height,
    ),
    rounding = CornerRounding(cornerRadius, smoothing),
  ).toPath().asComposePath()
}

// to use with BottomSheet implementation
@Suppress("unused")
private fun RoundedPolygon.Companion.squircleTop(
  width: Float,
  height: Float,
  cornerRadius: Float,
  @FloatRange(from = 0.0, to = 1.0) smoothing: Float,
): Path {
  if (width == 0f || height == 0f) {
    return Path()
  }
  @Suppress("ktlint:standard:argument-list-wrapping")
  return RoundedPolygon(
    vertices = floatArrayOf(
      0f, 0f,
      width, 0f,
      width, height,
      0f, height,
    ),
    perVertexRounding = listOf(
      CornerRounding(cornerRadius, smoothing),
      CornerRounding(cornerRadius, smoothing),
      CornerRounding.Unrounded,
      CornerRounding.Unrounded,
    ),
  ).toPath().asComposePath()
}

private class FigmaShape(
  private val radius: Dp,
  @FloatRange(from = 0.0, to = 1.0) private val smoothing: Float = 0.6f,
) : Shape {
  override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
    val squirclePath = RoundedPolygon.squircle(
      width = size.width,
      height = size.height,
      cornerRadius = with(density) { radius.toPx() },
      smoothing = smoothing,
    )
    return Outline.Generic(squirclePath)
  }
}
