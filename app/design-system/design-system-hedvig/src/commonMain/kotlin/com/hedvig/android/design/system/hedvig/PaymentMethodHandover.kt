package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.HelipadOutline
import com.hedvig.android.design.system.hedvig.icon.Plus

/** The size a [PaymentMethodTile]'s mark is drawn at. */
val PaymentMethodMarkSize = 39.dp

private val TileSize = 74.dp
private val BadgeSize = 24.dp
private val BadgeOffset = 8.dp

/**
 * The outlined tile a payment method is shown on while it is being connected or given up, with an
 * optional status badge on its top-right corner. [mark] is the method's brand mark, drawn at
 * [PaymentMethodMarkSize]; it inherits the tile's content colour, so a monochrome mark reads
 * correctly in both themes.
 */
@Composable
fun PaymentMethodTile(
  modifier: Modifier = Modifier,
  badge: @Composable (() -> Unit)? = null,
  mark: @Composable () -> Unit = { PaymentMethodPlusMark(Modifier.size(PaymentMethodMarkSize)) },
) {
  Box(modifier) {
    Surface(
      shape = HedvigTheme.shapes.cornerXXLarge,
      color = HedvigTheme.colorScheme.backgroundPrimary,
      contentColor = HedvigTheme.colorScheme.fillPrimary,
      modifier = Modifier
        .size(TileSize)
        .hedvigDropShadow(HedvigTheme.shapes.cornerXXLarge)
        .dashedBorder(
          color = HedvigTheme.colorScheme.borderSecondary,
          shape = HedvigTheme.shapes.cornerXXLarge,
        ),
    ) {
      Box(Modifier.size(TileSize), contentAlignment = Alignment.Center) {
        mark()
      }
    }
    if (badge != null) {
      Box(Modifier.align(Alignment.TopEnd).offset(BadgeOffset, -BadgeOffset)) {
        badge()
      }
    }
  }
}

/** The mark a [PaymentMethodTile] shows before a method has been picked. */
@Composable
fun PaymentMethodPlusMark(modifier: Modifier = Modifier) {
  Icon(
    imageVector = HedvigIcons.Plus,
    contentDescription = null,
    tint = HedvigTheme.colorScheme.fillSecondary,
    modifier = modifier,
  )
}

/** A circular status badge for the corner of a [PaymentMethodTile] or the Hedvig symbol. */
@Composable
fun PaymentMethodTileBadge(
  icon: ImageVector,
  containerColor: Color,
  contentColor: Color,
  modifier: Modifier = Modifier,
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .size(BadgeSize)
      .clip(CircleShape)
      .background(containerColor),
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = contentColor,
      modifier = Modifier.size(16.dp),
    )
  }
}

/**
 * The method being connected, linked by pulsing dots to the Hedvig symbol. [destinationBadge] marks
 * the symbol once the hand-off has landed.
 */
@Composable
fun PaymentMethodHandoverIllustration(
  modifier: Modifier = Modifier,
  destinationBadge: @Composable (() -> Unit)? = null,
  mark: @Composable () -> Unit = { PaymentMethodPlusMark(Modifier.size(PaymentMethodMarkSize)) },
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    PaymentMethodTile(mark = mark)
    ThreeDotsLoading()
    Box {
      Surface(
        shape = HedvigTheme.shapes.cornerXXLarge,
        color = HedvigTheme.colorScheme.fillBlack,
        contentColor = HedvigTheme.colorScheme.fillWhite,
        border = HedvigTheme.colorScheme.borderPrimary,
        modifier = Modifier.size(TileSize),
      ) {
        Box(Modifier.size(TileSize), contentAlignment = Alignment.Center) {
          Icon(
            imageVector = HedvigIcons.HelipadOutline,
            contentDescription = null,
            modifier = Modifier.size(65.dp),
          )
        }
      }
      if (destinationBadge != null) {
        Box(Modifier.align(Alignment.TopEnd).offset(BadgeOffset, -BadgeOffset)) {
          destinationBadge()
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewPaymentMethodHandoverIllustration() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PaymentMethodHandoverIllustration(modifier = Modifier.size(width = 210.dp, height = 74.dp))
    }
  }
}
