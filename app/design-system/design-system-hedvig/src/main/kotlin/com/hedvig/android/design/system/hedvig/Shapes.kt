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
data class Shapes internal constructor(
  val cornerXXLarge: Shape = ShapeTokens.CornerXXLarge,
  val cornerXLarge: Shape = ShapeTokens.CornerXLarge,
  val cornerLarge: Shape = ShapeTokens.CornerLarge,
  val cornerMedium: Shape = ShapeTokens.CornerMedium,
  val cornerSmall: Shape = ShapeTokens.CornerSmall,
  val cornerExtraSmall: Shape = ShapeTokens.CornerExtraSmall,
  val cornerNone: Shape = ShapeTokens.CornerNone,
)

internal fun Shapes.fromToken(token: ShapeKeyTokens): Shape {
  return when (token) {
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
