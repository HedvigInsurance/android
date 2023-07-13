package com.hedvig.android.core.ui.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.small.hedvig.Minus

/**
 * A card with a "Plus" icon at the end, which expands and turns that icon to a minus sign instead.
 */
@Composable
fun ExpandablePlusCard(
  isExpanded: Boolean,
  onClick: () -> Unit,
  content: @Composable RowScope.() -> Unit,
  expandedContent: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    onClick = onClick,
    colors = CardDefaults.outlinedCardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant,
      contentColor = MaterialTheme.colorScheme.onSurface,
    ),
    modifier = modifier,
  ) {
    Column {
      Spacer(Modifier.height(12.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 12.dp),
      ) {
        content()
        val iconRotation by animateFloatAsState(if (isExpanded) 0f else -90f)
        Box {
          val iconModifier = Modifier.size(16.dp)
          Icon(
            Icons.Hedvig.Minus,
            contentDescription = null,
            modifier = iconModifier.graphicsLayer {
              rotationZ = iconRotation
            },
          )
          Icon(
            Icons.Hedvig.Minus,
            contentDescription = null,
            modifier = iconModifier,
          )
        }
        Spacer(Modifier.width(4.dp))
      }
      Spacer(Modifier.height(12.dp))
      AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + expandVertically(clip = false, expandFrom = Alignment.Top),
        exit = fadeOut() + shrinkVertically(clip = false, shrinkTowards = Alignment.Top),
      ) {
        Column {
          expandedContent()
          Spacer(Modifier.height(12.dp))
        }
      }
    }
  }
}
