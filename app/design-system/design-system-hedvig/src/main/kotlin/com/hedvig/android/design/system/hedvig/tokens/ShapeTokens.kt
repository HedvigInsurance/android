package com.hedvig.android.design.system.hedvig.tokens

import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.tokens.FigmaShapeDirection.All
import com.hedvig.android.design.system.hedvig.tokens.FigmaShapeDirection.BottomOnly
import com.hedvig.android.design.system.hedvig.tokens.FigmaShapeDirection.TopOnly
import com.hedvig.android.design.system.hedvig.tokens.ShapeTokens.CornerLarge
import com.hedvig.android.design.system.hedvig.tokens.ShapeTokens.CornerMedium
import com.hedvig.android.design.system.hedvig.tokens.ShapeTokens.CornerNone
import com.hedvig.android.design.system.hedvig.tokens.ShapeTokens.CornerSmall
import com.hedvig.android.design.system.hedvig.tokens.ShapeTokens.CornerXLarge
import com.hedvig.android.design.system.hedvig.tokens.ShapeTokens.CornerXLargeTop
import com.hedvig.android.design.system.hedvig.tokens.ShapeTokens.CornerXSmall
import com.hedvig.android.design.system.hedvig.tokens.ShapeTokens.CornerXSmallBottom
import com.hedvig.android.design.system.hedvig.tokens.ShapeTokens.CornerXSmallTop
import com.hedvig.android.design.system.hedvig.tokens.ShapeTokens.CornerXXLarge

internal object ShapeTokens {
  val CornerXXLarge: Shape = FigmaShape(24.dp)
  val CornerXLarge: Shape = FigmaShape(16.dp)
  val CornerLarge: Shape = FigmaShape(12.dp)
  val CornerMedium: Shape = FigmaShape(10.dp)
  val CornerSmall: Shape = FigmaShape(8.dp)
  val CornerXSmall: Shape = FigmaShape(6.dp)
  val CornerNone: Shape = RectangleShape
  val CornerXLargeTop: Shape = FigmaShape(16.dp, figmaShapeDirection = TopOnly)
  val CornerXSmallTop: Shape = FigmaShape(6.dp, figmaShapeDirection = TopOnly)
  val CornerXSmallBottom: Shape = FigmaShape(6.dp, figmaShapeDirection = BottomOnly)
}

private class FigmaShape(
  private val radius: Dp,
  @FloatRange(from = 0.0, to = 1.0) private val smoothing: Float = 0.6f,
  private val figmaShapeDirection: FigmaShapeDirection = FigmaShapeDirection.All,
) : Shape {
  override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
    val squirclePath = RoundedPolygon.squircle(
      width = size.width,
      height = size.height,
      cornerRadius = with(density) { radius.toPx() },
      smoothing = smoothing,
      figmaShapeDirection = figmaShapeDirection,
    )
    return Outline.Generic(squirclePath)
  }
}

private fun RoundedPolygon.Companion.squircle(
  width: Float,
  height: Float,
  cornerRadius: Float,
  @FloatRange(from = 0.0, to = 1.0) smoothing: Float,
  figmaShapeDirection: FigmaShapeDirection,
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
    perVertexRounding = when (figmaShapeDirection) {
      All -> List(4) { CornerRounding(cornerRadius, smoothing) }
      TopOnly -> listOf(
        CornerRounding(cornerRadius, smoothing),
        CornerRounding(cornerRadius, smoothing),
        CornerRounding.Unrounded,
        CornerRounding.Unrounded,
      )

      BottomOnly -> listOf(
        CornerRounding.Unrounded,
        CornerRounding.Unrounded,
        CornerRounding(cornerRadius, smoothing),
        CornerRounding(cornerRadius, smoothing),
      )
    },
  ).toPath().asComposePath()
}

private enum class FigmaShapeDirection {
  All,
  TopOnly,
  BottomOnly,
}

@HedvigPreview
@Composable
private fun PreviewShapes() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      val shapes = listOf(
        "CornerXXLarge" to CornerXXLarge,
        "CornerXLarge" to CornerXLarge,
        "CornerLarge" to CornerLarge,
        "CornerMedium" to CornerMedium,
        "CornerSmall" to CornerSmall,
        "CornerXSmall" to CornerXSmall,
        "CornerNone" to CornerNone,
        "CornerXLargeTop" to CornerXLargeTop,
        "CornerXSmallTop" to CornerXSmallTop,
        "CornerXSmallBottom" to CornerXSmallBottom,
      )
      Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(8.dp),
      ) {
        for ((name, shape) in shapes) {
          Surface(shape = shape, modifier = Modifier.size(40.dp)) {
            HedvigText(
              text = name,
              fontSize = 4.sp,
              modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
            )
          }
        }
      }
    }
  }
}
