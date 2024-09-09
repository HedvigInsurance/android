package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import com.hedvig.android.compose.ui.UseSimplerShapesForOldAndroidVersions
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens
import com.hedvig.android.design.system.hedvig.tokens.ShapeTokens
import com.hedvig.android.design.system.hedvig.tokens.ShapeTokens.CornerTopOnlyXLarge

@Immutable
data class Shapes(
  val cornerXXLarge: Shape = ShapeTokens.CornerXXLarge,
  val cornerXLarge: Shape = ShapeTokens.CornerXLarge,
  val cornerLarge: Shape = ShapeTokens.CornerLarge,
  val cornerMedium: Shape = ShapeTokens.CornerMedium,
  val cornerSmall: Shape = ShapeTokens.CornerSmall,
  val cornerExtraSmall: Shape = ShapeTokens.CornerExtraSmall,
  val cornerNone: Shape = ShapeTokens.CornerNone,
  val roundedCornerXXLarge: Shape = ShapeTokens.RoundedCornerXXLarge,
  val roundedCornerXLarge: Shape = ShapeTokens.RoundedCornerXLarge,
  val roundedCornerLarge: Shape = ShapeTokens.RoundedCornerLarge,
  val roundedCornerMedium: Shape = ShapeTokens.RoundedCornerMedium,
  val roundedCornerSmall: Shape = ShapeTokens.RoundedCornerSmall,
  val roundedCornerExtraSmall: Shape = ShapeTokens.RoundedCornerExtraSmall,
  val roundedCornerTopOnlyXLarge: Shape = ShapeTokens.RoundedCornerTopOnlyXLarge,
)

internal fun Shapes.fromToken(token: ShapeKeyTokens, useSimplerShapes: Boolean): Shape {
  return if (useSimplerShapes) {
    when (token) {
      ShapeKeyTokens.CornerXXLarge -> roundedCornerXXLarge
      ShapeKeyTokens.CornerXLarge -> roundedCornerXLarge
      ShapeKeyTokens.CornerLarge -> roundedCornerLarge
      ShapeKeyTokens.CornerMedium -> roundedCornerMedium
      ShapeKeyTokens.CornerSmall -> roundedCornerSmall
      ShapeKeyTokens.CornerExtraSmall -> roundedCornerExtraSmall
      ShapeKeyTokens.CornerNone -> cornerNone
      ShapeKeyTokens.CornerTopOnlyXLarge -> roundedCornerTopOnlyXLarge
    }
  } else {
    when (token) {
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
}

internal val ShapeKeyTokens.value: Shape
  @Composable
  @ReadOnlyComposable
  get() = HedvigTheme.shapes.fromToken(this, UseSimplerShapesForOldAndroidVersions.current)

internal val LocalShapes = staticCompositionLocalOf { Shapes() }
