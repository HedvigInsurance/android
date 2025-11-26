package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Cubic
import androidx.graphics.shapes.RoundedPolygon
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
import kotlin.math.PI
import kotlin.math.atan2

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
  private val smoothing: Float = 0.6f,
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
  smoothing: Float,
  figmaShapeDirection: FigmaShapeDirection,
): Path {
  if (width == 0f || height == 0f) {
    return androidx.compose.ui.graphics.Path()
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
  ).toPath()
}

enum class FigmaShapeDirection {
  All,
  TopOnly,
  BottomOnly,
}

private fun RoundedPolygon.toPath(
  path: Path = Path(),
  startAngle: Int = 270,
  repeatPath: Boolean = false,
  closePath: Boolean = true,
): Path {
  pathFromCubics(
    path = path,
    startAngle = startAngle,
    repeatPath = repeatPath,
    closePath = closePath,
    cubics = cubics,
    rotationPivotX = centerX,
    rotationPivotY = centerY,
  )
  return path
}

private fun pathFromCubics(
  path: Path,
  startAngle: Int,
  repeatPath: Boolean,
  closePath: Boolean,
  cubics: List<Cubic>,
  rotationPivotX: Float,
  rotationPivotY: Float,
) {
  var first = true
  var firstCubic: Cubic? = null
  path.rewind()
  cubics.fastForEach {
    if (first) {
      path.moveTo(it.anchor0X, it.anchor0Y)
      if (startAngle != 0) {
        firstCubic = it
      }
      first = false
    }
    path.cubicTo(
      it.control0X,
      it.control0Y,
      it.control1X,
      it.control1Y,
      it.anchor1X,
      it.anchor1Y,
    )
  }
  if (repeatPath) {
    var firstInRepeat = true
    cubics.fastForEach {
      if (firstInRepeat) {
        path.lineTo(it.anchor0X, it.anchor0Y)
        firstInRepeat = false
      }
      path.cubicTo(
        it.control0X,
        it.control0Y,
        it.control1X,
        it.control1Y,
        it.anchor1X,
        it.anchor1Y,
      )
    }
  }

  if (closePath) path.close()

  if (startAngle != 0 && firstCubic != null) {
    val angleToFirstCubic =
      radiansToDegrees(
        atan2(
          y = cubics[0].anchor0Y - rotationPivotY,
          x = cubics[0].anchor0X - rotationPivotX,
        )
      )
    // Rotate the Path to to start from the given angle.
    path.transform(Matrix().apply { rotateZ(-angleToFirstCubic + startAngle) })
  }
}

private fun radiansToDegrees(radians: Float): Float {
  return (radians * 180.0 / PI).toFloat()
}
