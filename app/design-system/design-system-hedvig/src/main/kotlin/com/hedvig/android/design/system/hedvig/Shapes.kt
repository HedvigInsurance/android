package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens
import com.hedvig.android.design.system.hedvig.tokens.ShapeTokens
import com.hedvig.android.design.system.hedvig.tokens.ShapeTokens.CornerTopOnlyXLarge

@Immutable
data class Shapes(
  val cornerXXLarge: Shape = ShapeDefaults.CornerXXLarge,
  val cornerXLarge: Shape = ShapeDefaults.CornerXLarge,
  val cornerLarge: Shape = ShapeDefaults.CornerLarge,
  val cornerMedium: Shape = ShapeDefaults.CornerMedium,
  val cornerSmall: Shape = ShapeDefaults.CornerSmall,
  val cornerExtraSmall: Shape = ShapeDefaults.CornerExtraSmall,
  val cornerNone: Shape = ShapeDefaults.CornerNone,
)

object ShapeDefaults {
  val CornerXXLarge: Shape = ShapeTokens.CornerXXLarge
  val CornerXLarge: Shape = ShapeTokens.CornerXLarge
  val CornerLarge: Shape = ShapeTokens.CornerLarge
  val CornerMedium: Shape = ShapeTokens.CornerMedium
  val CornerSmall: Shape = ShapeTokens.CornerSmall
  val CornerExtraSmall: Shape = ShapeTokens.CornerExtraSmall
  val CornerNone: Shape = ShapeTokens.CornerNone
}

internal fun Shapes.fromToken(value: ShapeKeyTokens): Shape {
  return when (value) {
    ShapeKeyTokens.CornerXXLarge -> cornerXXLarge
    ShapeKeyTokens.CornerXLarge -> cornerXLarge
    ShapeKeyTokens.CornerLarge -> cornerLarge
    ShapeKeyTokens.CornerMedium -> cornerMedium
    ShapeKeyTokens.CornerSmall -> cornerSmall
    ShapeKeyTokens.CornerExtraSmall -> cornerExtraSmall
    ShapeKeyTokens.CornerNone -> cornerNone
    ShapeKeyTokens.CornerTopOnlyXLarge -> CornerTopOnlyXLarge
  }
}

internal val ShapeKeyTokens.value: Shape
  @Composable
  @ReadOnlyComposable
  get() = HedvigTheme.shapes.fromToken(this)

internal val LocalShapes = staticCompositionLocalOf { Shapes() }
