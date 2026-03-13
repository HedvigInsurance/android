package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.hedvigDropShadow(
  shape: Shape = HedvigTheme.shapes.cornerLarge
): Modifier {
  return this.then(
    if (isSystemInDarkTheme()) {
      Modifier
    } else {
      Modifier
        .dropShadow(
          shape,
          Shadow(
            radius = 10.dp,
            offset = DpOffset(0.dp, 4.dp),
            color = HedvigTheme.colorScheme.borderPrimary,
            spread = (-2).dp,
          ),
        ).dropShadow(
          HedvigTheme.shapes.cornerLarge,
          Shadow(
            radius = 2.dp,
            offset = DpOffset(0.dp, 2.dp),
            color = HedvigTheme.colorScheme.borderSecondary,
            spread = (-1).dp,
          ),
        )
    },
  )
}
