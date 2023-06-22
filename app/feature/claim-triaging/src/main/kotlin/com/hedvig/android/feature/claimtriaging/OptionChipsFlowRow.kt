package com.hedvig.android.feature.claimtriaging

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import arrow.core.identity
import com.hedvig.android.core.designsystem.material3.motion.MotionTokens
import com.hedvig.android.core.designsystem.material3.onTypeContainer
import com.hedvig.android.core.designsystem.material3.squircle
import com.hedvig.android.core.designsystem.material3.typeContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun <T> OptionChipsFlowRow(
  items: ImmutableList<T>,
  itemDisplayName: (T) -> String,
  selectedItem: T?,
  onItemClick: (T) -> Unit,
  modifier: Modifier = Modifier,
) {
  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    for (item in items) {
      key(item) {
        val interactionSource = remember { MutableInteractionSource() }
        val scale = remember { Animatable(1f) }
        Box(
          // TODO replace with space on the flow row itself https://kotlinlang.slack.com/archives/CJLTWPH7S/p1687442185827989?thread_ts=1679515354.462029&cid=CJLTWPH7S
          modifier = Modifier.padding(bottom = 8.dp),
        ) {
          val surfaceColor by animateColorAsState(
            if (selectedItem == item) {
              MaterialTheme.colorScheme.typeContainer.compositeOver(MaterialTheme.colorScheme.background)
            } else {
              MaterialTheme.colorScheme.surface
            },
          )
          val contentColor by animateColorAsState(
            if (selectedItem == item) {
              MaterialTheme.colorScheme.onTypeContainer.compositeOver(surfaceColor)
            } else {
              MaterialTheme.colorScheme.onSurface
            },
          )
          LaunchedEffect(interactionSource) {
            interactionSource
              .interactions
              .filterIsInstance<PressInteraction.Release>()
              .collectLatest {
                scale.animateTo(
                  targetValue = 1.05f,
                  animationSpec = tween(durationMillis = 150, easing = MotionTokens.EasingStandardCubicBezier),
                )
                scale.animateTo(
                  targetValue = 1f,
                  animationSpec = tween(durationMillis = 150, easing = MotionTokens.EasingStandardCubicBezier),
                )
              }
          }
          Box(
            modifier = Modifier
              .matchParentSize()
              .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
              }
              .clip(MaterialTheme.shapes.squircle)
              .background(surfaceColor, MaterialTheme.shapes.squircle)
              .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onItemClick(item) },
              ),
          )
          CompositionLocalProvider(LocalContentColor provides contentColor) {
            Text(
              text = itemDisplayName(item),
              style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
              ),
              maxLines = 1,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
          }
        }
      }
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewOptionChipsFlowRow() {
  HedvigTheme(useNewColorScheme = true) {
    Surface(color = MaterialTheme.colorScheme.background) {
      Box(modifier = Modifier.padding(16.dp)) {
        val items = remember {
          List(12) {
            val displayName = buildString { repeat((4..14).random()) { append(('a'..'z').random()) } }
            displayName
          }.toImmutableList()
        }
        OptionChipsFlowRow(
          items,
          ::identity,
          items[3],
          {},
        )
      }
    }
  }
}
