@file:Suppress("UnusedReceiverParameter")

package com.hedvig.android.core.designsystem.material3

import androidx.annotation.FloatRange
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
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
import com.hedvig.android.core.designsystem.component.tokens.HedvigShapeKeyTokens

// Take shapes from existing theme setup
// https://github.com/HedvigInsurance/android/blob/ced77986fac0fd7867c8e24ba05d0176a112050e/app/src/main/res/values/theme.xml#L27-L33
// https://github.com/HedvigInsurance/android/blob/0dfcbd61bd6b4f4b0d5bbd93e339deff3e15b5a9/app/src/main/res/values/shape_themes.xml#L4-L10
internal val HedvigShapes: Shapes
  @Composable
  @ReadOnlyComposable
  get() = MaterialTheme.shapes.copy(
    medium = RoundedCornerShape(8.0.dp),
    large = RoundedCornerShape(8.0.dp),
  )

private fun RoundedPolygon.Companion.squircle(
  width: Float,
  height: Float,
  cornerRadius: Float,
  @FloatRange(from = 0.0, to = 1.0) smoothing: Float,
): android.graphics.Path {
  if (width == 0f || height == 0f) {
    return android.graphics.Path()
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
  ).toPath()
}

internal class FigmaShape(
  private val radius: Dp,
  @FloatRange(from = 0.0, to = 1.0) private val smoothing: Float = 1f,
) : Shape {
  override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
    val squirclePath = RoundedPolygon.squircle(
      width = size.width,
      height = size.height,
      cornerRadius = with(density) { radius.toPx() },
      smoothing = smoothing,
    )
    return Outline.Generic(squirclePath.asComposePath())
  }
}

private val SquircleExtraSmall = FigmaShape(8.dp)
private val SquircleExtraSmallTop = FigmaShape(8.dp).top()
private val SquircleSmall = FigmaShape(10.dp)
private val SquircleMedium = FigmaShape(12.dp)
private val SquircleLarge = FigmaShape(16.dp)
private val SquircleLargeTop = FigmaShape(16.dp).top()
private val SquircleExtraLarge = FigmaShape(24.dp)
private val SquircleExtraLargeTop = FigmaShape(24.dp).top()

val Shapes.squircleExtraSmall: Shape
  get() = SquircleExtraSmall
val Shapes.squircleSmall: Shape
  get() = SquircleSmall
val Shapes.squircleMedium: Shape
  get() = SquircleMedium
val Shapes.squircleLarge: Shape
  get() = SquircleLarge
val Shapes.squircleLargeTop: Shape
  get() = SquircleLargeTop

/**
 * Turns the shape into one where only the top corners apply, by combining the path with a square path at the bottom.
 * This is only to be used with Outline.Generic paths, otherwise it just crashes.
 */
private fun Shape.top(): Shape = object : Shape {
  override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
    val existingShapePath = (this@top.createOutline(size, layoutDirection, density) as Outline.Generic).path
    val flatBottomShape = Path().apply {
      moveTo(0f, size.height / 2)
      lineTo(0f, size.height)
      lineTo(size.width, size.height)
      lineTo(size.width, size.height / 2)
      close()
    }
    return Outline.Generic(
      Path.combine(
        operation = PathOperation.Union,
        path1 = flatBottomShape,
        path2 = existingShapePath,
      ),
    )
  }
}

/**
 * Helper function for component shape tokens. Here is an example on how to use component color
 * tokens:
 * ``MaterialTheme.shapes.fromToken(FabPrimarySmallTokens.ContainerShape)``
 */
internal fun Shapes.fromToken(token: HedvigShapeKeyTokens): Shape {
  return when (token) {
    HedvigShapeKeyTokens.CornerExtraLarge -> SquircleExtraLarge
    HedvigShapeKeyTokens.CornerExtraLargeTop -> SquircleExtraLargeTop
    HedvigShapeKeyTokens.CornerExtraSmall -> SquircleExtraSmall
    HedvigShapeKeyTokens.CornerExtraSmallTop -> SquircleExtraSmallTop
    HedvigShapeKeyTokens.CornerFull -> CircleShape
    HedvigShapeKeyTokens.CornerLarge -> SquircleLarge
    HedvigShapeKeyTokens.CornerLargeTop -> SquircleLargeTop
    HedvigShapeKeyTokens.CornerMedium -> SquircleMedium
    HedvigShapeKeyTokens.CornerNone -> RectangleShape
    HedvigShapeKeyTokens.CornerSmall -> SquircleSmall
  }
}

/** Converts a shape token key to the local shape provided by the theme */
@Composable
@ReadOnlyComposable
internal fun HedvigShapeKeyTokens.toShape(): Shape {
  return MaterialTheme.shapes.fromToken(this)
}
