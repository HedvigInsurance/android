package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.hedvigDropShadow(shape: Shape = HedvigTheme.shapes.cornerLarge): Modifier {
  return this.then(
    if (isSystemInDarkTheme()) {
      Modifier
    } else {
      Modifier
        .dropShadow(
          shape,
          Shadow(
            radius = SoftShadowRadius,
            offset = DpOffset(0.dp, SoftShadowOffsetY),
            color = HedvigTheme.colorScheme.shadowSoftLightOnly,
            spread = SoftShadowSpread,
          ),
        ).dropShadow(
          shape,
          Shadow(
            radius = 2.dp,
            offset = DpOffset(0.dp, 2.dp),
            color = HedvigTheme.colorScheme.shadowLightOnly,
            spread = (-1).dp,
          ),
        )
    },
  )
}

private val SoftShadowRadius = 10.dp
private val SoftShadowOffsetY = 4.dp
private val SoftShadowSpread = (-2).dp

/**
 * How far [hedvigDropShadow] reaches outside the shape it is drawn under, at most: the soft shadow's
 * blur plus its downward offset, less its negative spread. The crisp shadow sits well inside that.
 */
val HedvigDropShadowExtent: Dp = SoftShadowRadius + SoftShadowOffsetY + SoftShadowSpread
