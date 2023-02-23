package com.hedvig.android.core.designsystem.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Our mapping of m3 style to our card is that we always want the outlined card colors, as they closely resemble how it
 * was on m2 too. For elevation, we want the specs of elevated cards when there is elevation, and the specs of the
 * filled card when there is no elevation, making it feel like part of the surface.
 * See more details about m3 card specs at: https://m3.material.io/components/cards/overview
 */
@Composable
fun HedvigCard(
  modifier: Modifier = Modifier,
  shape: Shape = CardDefaults.shape,
  colors: CardColors = CardDefaults.outlinedCardColors(),
  elevation: HedvigCardElevation = HedvigCardElevation.NoElevation,
  border: BorderStroke? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  Card(
    modifier = modifier,
    shape = shape,
    colors = colors,
    elevation = when (elevation) {
      HedvigCardElevation.NoElevation -> CardDefaults.cardElevation()
      is HedvigCardElevation.Elevated -> CardDefaults.elevatedCardElevation(elevation.elevation)
    },
    border = border,
    content = content,
  )
}

sealed interface HedvigCardElevation {
  object NoElevation : HedvigCardElevation
  class Elevated(val elevation: Dp = 0.dp) : HedvigCardElevation
}
