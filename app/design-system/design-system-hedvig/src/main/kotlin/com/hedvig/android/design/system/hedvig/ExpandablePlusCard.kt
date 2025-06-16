package com.hedvig.android.design.system.hedvig

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Minus
import com.hedvig.android.design.system.hedvig.tokens.PerilCommonTokens

@Composable
fun ExpandablePlusCard(
  isExpanded: Boolean,
  onClick: () -> Unit,
  contentPadding: PaddingValues,
  content: @Composable RowScope.() -> Unit,
  expandedContent: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .clip(PerilCommonTokens.ContainerShape.value)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(
          bounded = true,
          radius = 1000.dp,
        ),
        onClick = onClick,
        //role = Role.DropdownList,
          onClickLabel =  if (isExpanded) "Collapse" else "Expand"
      )
      .semantics {
        this.contentDescription = "Expandable item"
    //    this.stateDescription = if (isExpanded) "Expanded" else "Collapsed"
      }
    ,
  ) {
    Column(
      modifier = Modifier
//        .semantics(true){
//          this.isTraversalGroup
//        }
        .padding(contentPadding),
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            content()
          }
        },
        endSlot = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
          ) {
            val halfRotation by animateFloatAsState(
              targetValue = if (isExpanded) 0f else -90f,
              animationSpec = PerilCommonTokens.IconAnimationSpec,
            )
            val fullRotation by animateFloatAsState(
              targetValue = if (isExpanded) 0f else -180f,
              animationSpec = PerilCommonTokens.IconAnimationSpec,
            )
            Box {
              val iconModifier = Modifier.size(PerilCommonTokens.PlusIconSize)
              Icon(
                HedvigIcons.Minus,
                contentDescription = EmptyContentDescription,
                modifier = iconModifier.graphicsLayer {
                  rotationZ = halfRotation
                },
              )
              Icon(
                HedvigIcons.Minus,
                contentDescription = EmptyContentDescription,
                modifier = iconModifier.graphicsLayer {
                  rotationZ = fullRotation
                },
              )
            }
          }
        },
        spaceBetween = 4.dp,
      )
      AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + expandVertically(clip = false, expandFrom = Alignment.Top),
        exit = fadeOut() + shrinkVertically(clip = false, shrinkTowards = Alignment.Top),
      ) {
        expandedContent()
      }
    }
  }
}
