package com.hedvig.android.core.designsystem.material3

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.tokens.HedvigShapeKeyTokens
import com.hedvig.android.core.designsystem.newtheme.SquircleShape

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

@Suppress("UnusedReceiverParameter")
val Shapes.squircle: Shape
  get() = SquircleShape

internal fun CornerBasedShape.top(): CornerBasedShape {
  return copy(bottomStart = CornerSize(0.0.dp), bottomEnd = CornerSize(0.0.dp))
}

internal fun CornerBasedShape.end(): CornerBasedShape {
  return copy(topStart = CornerSize(0.0.dp), bottomStart = CornerSize(0.0.dp))
}

/**
 * Helper function for component shape tokens. Here is an example on how to use component color
 * tokens:
 * ``MaterialTheme.shapes.fromToken(FabPrimarySmallTokens.ContainerShape)``
 */
internal fun Shapes.fromToken(value: HedvigShapeKeyTokens): Shape {
  return when (value) {
    HedvigShapeKeyTokens.CornerExtraLarge -> extraLarge
    HedvigShapeKeyTokens.CornerExtraLargeTop -> extraLarge.top()
    HedvigShapeKeyTokens.CornerExtraSmall -> extraSmall
    HedvigShapeKeyTokens.CornerExtraSmallTop -> extraSmall.top()
    HedvigShapeKeyTokens.CornerFull -> CircleShape
    HedvigShapeKeyTokens.CornerLarge -> large
    HedvigShapeKeyTokens.CornerLargeEnd -> large.end()
    HedvigShapeKeyTokens.CornerLargeTop -> large.top()
    HedvigShapeKeyTokens.CornerMedium -> medium
    HedvigShapeKeyTokens.CornerNone -> RectangleShape
    HedvigShapeKeyTokens.CornerSmall -> small
    HedvigShapeKeyTokens.Squircle -> SquircleShape
  }
}

/** Converts a shape token key to the local shape provided by the theme */
@Composable
@ReadOnlyComposable
internal fun HedvigShapeKeyTokens.toShape(): Shape {
  return MaterialTheme.shapes.fromToken(this)
}
