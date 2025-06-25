package com.hedvig.android.design.system.hedvig

import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
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
import com.hedvig.android.design.system.hedvig.FigmaShapeDirection.All
import com.hedvig.android.design.system.hedvig.FigmaShapeDirection.BottomOnly
import com.hedvig.android.design.system.hedvig.FigmaShapeDirection.TopOnly
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.CornerLarge
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.CornerMedium
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.CornerNone
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.CornerSmall
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.CornerXLarge
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.CornerXLargeTop
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.CornerXSmall
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.CornerXSmallBottom
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.CornerXSmallTop
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.CornerXXLarge

@Immutable
data class Shapes internal constructor(
  val cornerXXLarge: Shape = FigmaShape(24.dp),
  val cornerXLarge: Shape = FigmaShape(16.dp),
  val cornerLarge: Shape = FigmaShape(12.dp),
  val cornerMedium: Shape = FigmaShape(10.dp),
  val cornerSmall: Shape = FigmaShape(8.dp),
  val cornerXSmall: Shape = FigmaShape(6.dp),
  val cornerNone: Shape = RectangleShape,
  val cornerXLargeTop: Shape = FigmaShape(16.dp, figmaShapeDirection = TopOnly),
  val cornerXLargeBottom: Shape = FigmaShape(16.dp, figmaShapeDirection = BottomOnly),
  val cornerXSmallTop: Shape = FigmaShape(6.dp, figmaShapeDirection = TopOnly),
  val cornerXSmallBottom: Shape = FigmaShape(6.dp, figmaShapeDirection = BottomOnly),
)

internal val ShapeKeyTokens.value: Shape
  @Composable
  @ReadOnlyComposable
  get() = HedvigTheme.shapes.fromToken(this)

internal fun Shapes.fromToken(token: ShapeKeyTokens): Shape {
  return when (token) {
    CornerXXLarge -> cornerXXLarge
    CornerXLarge -> cornerXLarge
    CornerLarge -> cornerLarge
    CornerMedium -> cornerMedium
    CornerSmall -> cornerSmall
    CornerXSmall -> cornerXSmall
    CornerNone -> cornerNone
    CornerXLargeTop -> cornerXLargeTop
    CornerXSmallTop -> cornerXSmallTop
    CornerXSmallBottom -> cornerXSmallBottom
  }
}

internal val LocalShapes = staticCompositionLocalOf { Shapes() }

private class FigmaShape(
  private val radius: Dp,
  @FloatRange(from = 0.0, to = 1.0) private val smoothing: Float = 0.6f,
  private val figmaShapeDirection: FigmaShapeDirection = All,
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
          Surface(shape = shape.value, modifier = Modifier.size(40.dp)) {
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
