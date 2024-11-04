package com.hedvig.android.design.system.hedvig

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.CornerExtraSmall
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.CornerLarge
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.CornerMedium
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.CornerNone
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.CornerSmall
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.CornerXLarge
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.CornerXXLarge
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.SmallBottomCorners
import com.hedvig.android.design.system.hedvig.tokens.ShapeKeyTokens.SmallTopCorners
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
  val smallTopCorners: Shape = ShapeTokens.SmallTopCorners,
  val smallBottomCorners: Shape = ShapeTokens.SmallBottomCorners,
)

internal fun Shapes.fromToken(token: ShapeKeyTokens): Shape {
  return when (token) {
    CornerXXLarge -> cornerXXLarge
    CornerXLarge -> cornerXLarge
    CornerLarge -> cornerLarge
    CornerMedium -> cornerMedium
    CornerSmall -> cornerSmall
    CornerExtraSmall -> cornerExtraSmall
    CornerNone -> cornerNone
    ShapeKeyTokens.CornerTopOnlyXLarge -> CornerTopOnlyXLarge
    ShapeKeyTokens.SmallTopCorners -> ShapeTokens.SmallTopCorners
    ShapeKeyTokens.SmallBottomCorners -> ShapeTokens.SmallBottomCorners
  }
}

internal val ShapeKeyTokens.value: Shape
  @Composable
  @ReadOnlyComposable
  get() = HedvigTheme.shapes.fromToken(this)

internal val LocalShapes = staticCompositionLocalOf { Shapes() }
