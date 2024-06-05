package com.hedvig.android.core.designsystem.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.squircleMedium

/**
 * Our mapping of m3 style to our card is that we always want the outlined card colors, as they closely resemble how it
 * was on m2 too. For elevation, we want the specs of elevated cards when there is elevation, and the specs of the
 * filled card when there is no elevation, making it feel like part of the surface.
 * See more details about m3 card specs at: https://m3.material.io/components/cards/overview
 */
@Composable
fun HedvigCard(
  modifier: Modifier = Modifier,
  shape: Shape = MaterialTheme.shapes.squircleMedium,
  colors: CardColors = CardDefaults.outlinedCardColors(),
  elevation: HedvigCardElevation = HedvigCardElevation.NoElevation,
  border: BorderStroke? = null,
  content: @Composable () -> Unit,
) {
  HedvigCard(
    onClick = null,
    modifier = modifier,
    shape = shape,
    colors = colors,
    elevation = elevation,
    border = border,
    enabled = true,
    content = content,
  )
}

/**
 * Same as [HedvigCard] but is clickable.
 */
@Composable
fun HedvigCard(
  onClick: (() -> Unit)?,
  modifier: Modifier = Modifier,
  shape: Shape = MaterialTheme.shapes.squircleMedium,
  colors: CardColors = CardDefaults.outlinedCardColors(),
  elevation: HedvigCardElevation = HedvigCardElevation.NoElevation,
  border: BorderStroke? = null,
  enabled: Boolean = true,
  content: @Composable () -> Unit,
) {
  if (onClick == null) {
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
  } else {
    Card(
      onClick = onClick,
      enabled = enabled,
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
}

sealed interface HedvigCardElevation {
  object NoElevation : HedvigCardElevation

  class Elevated(val elevation: Dp = 0.dp) : HedvigCardElevation
}

/**
 * A copy of the `androidx.compose.material3.Card` but does not wrap the content in a Column which ends up not
 * propagating the minimum size constraints that are set to the card itself.
 */
@Composable
private fun Card(
  modifier: Modifier = Modifier,
  shape: Shape = CardDefaults.shape,
  colors: CardColors = CardDefaults.cardColors(),
  elevation: CardElevation = CardDefaults.cardElevation(),
  border: BorderStroke? = null,
  content: @Composable () -> Unit,
) {
  @Suppress("INVISIBLE_MEMBER")
  Surface(
    modifier = modifier,
    shape = shape,
   // color = colors.containerColor(enabled = true), //todo: remove!!
    // contentColor = colors.contentColor(enabled = true), //todo: remove!!
  //  tonalElevation = elevation.tonalElevation(enabled = true), //todo: remove!!
  //  shadowElevation = elevation.shadowElevation(enabled = true, interactionSource = null).value, //todo: remove!!
    border = border,
    //todo: REMOVE EXPERIMENTAL VALUES HERE!!!!
  ) {
    content()
  }
}

/**
 * Same as [Card] but is clickable
 */
@Composable
private fun Card(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = CardDefaults.shape,
  colors: CardColors = CardDefaults.cardColors(),
  elevation: CardElevation = CardDefaults.cardElevation(),
  border: BorderStroke? = null,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  content: @Composable () -> Unit,
) {
  @Suppress("INVISIBLE_MEMBER")
  Surface(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    shape = shape,
//    color = colors.containerColor(enabled),
//    contentColor = colors.contentColor(enabled),
//    tonalElevation = elevation.tonalElevation(enabled),
//    shadowElevation = elevation.shadowElevation(enabled, interactionSource).value, //todo: remove!!
    border = border,
    interactionSource = interactionSource,
    content = content,
  )
}
