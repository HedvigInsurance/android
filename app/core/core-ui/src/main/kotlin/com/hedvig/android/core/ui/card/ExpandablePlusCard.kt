package com.hedvig.android.core.ui.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
    modifier = modifier,
  ) {
    Column {
      Spacer(Modifier.height(12.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 12.dp),
      ) {
        content()
        val halfRotation by animateFloatAsState(
          targetValue = if (isExpanded) 0f else -90f,
          animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        )
        val fullRotation by animateFloatAsState(
          targetValue = if (isExpanded) 0f else -180f,
          animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        )
        Spacer(Modifier.width(8.dp))
        Box {
          val iconModifier = Modifier.size(16.dp)
          Icon(
            Icons.Hedvig.Minus,
            contentDescription = null,
            modifier = iconModifier.graphicsLayer {
              rotationZ = halfRotation
            },
          )
          Icon(
            Icons.Hedvig.Minus,
            contentDescription = null,
            modifier = iconModifier.graphicsLayer {
              rotationZ = fullRotation
            },
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

/**
 * A simpler version of [ExpandablePlusCard] which only accepts two strings for the title and for the expanded content.
 */
@Composable
fun ExpandablePlusCard(
  isExpanded: Boolean,
  onClick: () -> Unit,
  titleText: String,
  expandedText: String,
  modifier: Modifier = Modifier,
) {
  ExpandablePlusCard(
    isExpanded = isExpanded,
    onClick = onClick,
    content = {
      Text(
        text = titleText,
        modifier = Modifier.weight(1f, true),
      )
    },
    expandedContent = {
      Text(
        text = expandedText,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 12.dp, end = 32.dp),
      )
    },
    modifier = modifier,
  )
}
