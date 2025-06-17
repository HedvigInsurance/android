package com.hedvig.android.ui.claimflow

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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LocalContentColor
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.tokens.MotionTokens
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
    modifier = modifier
      .graphicsLayer {
        scaleX = showChipAnimatable.value
        scaleY = showChipAnimatable.value
      }
      .clearAndSetSemantics {
        val name = itemDisplayName(item)
        contentDescription = name
        selected = isSelected
        this.onClick {
          onItemClick(item)
          true
        }
      },
    contentAlignment = Alignment.Center,
  ) {
    val surfaceColor by animateColorAsState(
      if (isSelected) {
        HedvigTheme.colorScheme.signalGreenFill.compositeOver(HedvigTheme.colorScheme.backgroundPrimary)
      } else {
        HedvigTheme.colorScheme.surfacePrimary
      },
    )
    val contentColor by animateColorAsState(
      if (isSelected) {
        HedvigTheme.colorScheme.signalGreenText.compositeOver(surfaceColor)
      } else {
        HedvigTheme.colorScheme.textPrimary
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
        .clip(HedvigTheme.shapes.cornerLarge)
        .background(surfaceColor, HedvigTheme.shapes.cornerLarge)
        .clickable(
          interactionSource = interactionSource,
          indication = null,
          onClick = { onItemClick(item) },
        ),
    )
    CompositionLocalProvider(LocalContentColor provides contentColor) {
      HedvigText(
        text = itemDisplayName(item),
        style = HedvigTheme.typography.bodySmall,
        maxLines = 1,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewHedvigChip() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      HedvigChip(
        item = "Item",
        itemDisplayName = { it },
        isSelected = true,
        onItemClick = {},
        showChipAnimatable = remember { Animatable(1f) },
      )
    }
  }
}
