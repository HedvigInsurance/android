package com.hedvig.android.feature.payin.account.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.dashedBorder
import com.hedvig.android.design.system.hedvig.hedvigDropShadow

/** The size a [PayinMethodTile]'s mark is drawn at. */
internal val PayinMethodMarkSize = 39.dp

private val TileSize = 74.dp
private val BadgeSize = 24.dp

/**
 * The outlined tile a payin method is shown on while it is being connected or given up, with an
 * optional status badge on its top-right corner.
 */
@Composable
internal fun PayinMethodTile(
  modifier: Modifier = Modifier,
  badge: @Composable (() -> Unit)? = null,
  mark: @Composable () -> Unit,
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
      Box(
        Modifier
          .align(Alignment.TopEnd)
          .offset(x = 8.dp, y = (-8).dp),
      ) {
        badge()
      }
    }
  }
}

/** A circular status badge for [PayinMethodTile]'s corner. */
@Composable
internal fun PayinMethodTileBadge(
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
      contentDescription = EmptyContentDescription,
      tint = contentColor,
      modifier = Modifier.size(16.dp),
    )
  }
}
