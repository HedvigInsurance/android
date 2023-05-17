package com.hedvig.android.feature.changeaddress.ui.offer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import hedvig.resources.R

@Composable
internal fun ExpandablePlusRow(
  isExpanded: Boolean,
  onClick: () -> Unit,
  content: @Composable RowScope.() -> Unit,
  expandedContent: @Composable ColumnScope.() -> Unit,
) {
  HedvigCard(
    onClick = onClick,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 16.dp).padding(top = 12.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        content()
        val iconRotation by animateFloatAsState(if (isExpanded) 90f else 0f)
        Icon(
          painterResource(R.drawable.ic_plus_sign),
          contentDescription = null,
          modifier = Modifier
            .size(30.dp)
            .wrapContentSize()
            .graphicsLayer {
              rotationZ = iconRotation
            },
        )
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
