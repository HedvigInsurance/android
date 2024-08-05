package com.hedvig.android.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.motion.MotionTokens
import com.hedvig.android.core.designsystem.material3.onTypeContainer
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.material3.typeContainer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance

@Composable
fun <T> HedvigChip(
  item: T,
  itemDisplayName: (T) -> String,
  isSelected: Boolean,
  onItemClick: (T) -> Unit,
  modifier: Modifier = Modifier,
  showChipAnimatable: Animatable<Float, AnimationVector1D>,
) {
  Box(
    // TODO replace with space on the flow row itself https://kotlinlang.slack.com/archives/CJLTWPH7S/p1687442185827989?thread_ts=1679515354.462029&cid=CJLTWPH7S
    modifier = modifier
      .padding(bottom = 8.dp)
      .graphicsLayer {
        scaleX = showChipAnimatable.value
        scaleY = showChipAnimatable.value
      },
    contentAlignment = Alignment.Center,
  ) {
    val surfaceColor by animateColorAsState(
      if (isSelected) {
        MaterialTheme.colorScheme.typeContainer.compositeOver(MaterialTheme.colorScheme.background)
      } else {
        MaterialTheme.colorScheme.surface
      },
    )
    val contentColor by animateColorAsState(
      if (isSelected) {
        MaterialTheme.colorScheme.onTypeContainer.compositeOver(surfaceColor)
      } else {
        MaterialTheme.colorScheme.onSurface
      },
    )
    val backgroundScale = remember { Animatable(1f) }
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
      interactionSource
        .interactions
        .filterIsInstance<PressInteraction.Release>()
        .collectLatest {
          backgroundScale.animateTo(
            targetValue = 1.05f,
            animationSpec = tween(
              durationMillis = MotionTokens.DurationShort3.toInt(),
              easing = MotionTokens.EasingStandardCubicBezier,
            ),
          )
          backgroundScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
              durationMillis = MotionTokens.DurationShort3.toInt(),
              easing = MotionTokens.EasingStandardCubicBezier,
            ),
          )
        }
    }
    Box(
      modifier = Modifier
        .matchParentSize()
        .graphicsLayer {
          scaleX = backgroundScale.value
          scaleY = backgroundScale.value
        }
        .clip(MaterialTheme.shapes.squircleMedium)
        .background(surfaceColor, MaterialTheme.shapes.squircleMedium)
        .clickable(
          interactionSource = interactionSource,
          indication = null,
          onClick = { onItemClick(item) },
        ),
    )
    CompositionLocalProvider(LocalContentColor provides contentColor) {
      Text(
        text = itemDisplayName(item),
        style = MaterialTheme.typography.bodyLarge,
        maxLines = 1,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
      )
    }
  }
}
