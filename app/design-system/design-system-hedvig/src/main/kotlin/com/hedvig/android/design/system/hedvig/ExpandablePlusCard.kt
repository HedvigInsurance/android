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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Minus
import com.hedvig.android.design.system.hedvig.tokens.PerilCommonTokens
import hedvig.resources.R

@Composable
fun ExpandablePlusCard(
  isExpanded: Boolean,
  onClick: () -> Unit,
  contentPadding: PaddingValues,
  content: @Composable RowScope.() -> Unit,
  expandedContent: @Composable () -> Unit,
  modifier: Modifier = Modifier,
) {
  val collapsedStateDescription = stringResource(R.string.TALKBACK_EXPANDABLE_STATE_COLLAPSED)
  val expandedStateDescription = stringResource(R.string.TALKBACK_EXPANDABLE_STATE_EXPANDED)
  val collapseClickLabel = stringResource(R.string.TALKBACK_EXPANDABLE_CLICK_LABEL_COLLAPSE)
  val expandClickLabel = stringResource(R.string.TALKBACK_EXPANDABLE_CLICK_LABEL_EXPAND)
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
        onClickLabel = if (isExpanded) collapseClickLabel else expandClickLabel,
      )
      .semantics {
        this.stateDescription = if (isExpanded) expandedStateDescription else collapsedStateDescription
      },
  ) {
    Column(modifier = Modifier.padding(contentPadding)) {
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
        modifier = Modifier.semantics(mergeDescendants = true) {},
      ) {
        expandedContent()
      }
    }
  }
}
