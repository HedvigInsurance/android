package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics

@Composable
@NonRestartableComposable
fun Surface(
  modifier: Modifier = Modifier,
  shape: Shape = RectangleShape,
  color: Color = HedvigTheme.colorScheme.surfacePrimary,
  contentColor: Color = contentColorFor(color),
  border: BorderStroke? = null,
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(LocalContentColor provides contentColor) {
    Box(
      modifier = modifier
        .surface(
          shape = shape,
          backgroundColor = color,
          border = border,
        )
        .semantics(mergeDescendants = false) {
          isTraversalGroup = true
        }
        .pointerInput(Unit) {},
      propagateMinConstraints = true,
    ) {
      content()
    }
  }
}

@Composable
@NonRestartableComposable
fun Surface(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = RectangleShape,
  color: Color = HedvigTheme.colorScheme.surfacePrimary,
  contentColor: Color = contentColorFor(color),
  border: BorderStroke? = null,
  interactionSource: MutableInteractionSource? = null,
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(LocalContentColor provides contentColor) {
    Box(
      modifier = modifier
        .minimumInteractiveComponentSize()
        .surface(
          shape = shape,
          backgroundColor = color,
          border = border,
        )
        .clickable(
          interactionSource = interactionSource,
          indication = LocalIndication.current,
          enabled = enabled,
          onClick = onClick,
        ),
      propagateMinConstraints = true,
    ) {
      content()
    }
  }
}

@Stable
private fun Modifier.surface(shape: Shape, backgroundColor: Color, border: BorderStroke?) = this
  .then(if (border != null) Modifier.border(border, shape) else Modifier)
  .background(color = backgroundColor, shape = shape)
  .clip(shape)
