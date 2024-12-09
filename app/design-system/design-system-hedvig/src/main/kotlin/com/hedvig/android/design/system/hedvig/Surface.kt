package com.hedvig.android.design.system.hedvig

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
@NonRestartableComposable
fun Surface(
  modifier: Modifier = Modifier,
  shape: Shape = RectangleShape,
  color: Color = HedvigTheme.colorScheme.surfacePrimary,
  contentColor: Color = contentColorFor(color),
  border: Color? = null,
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(LocalContentColor provides contentColor) {
    Box(
      modifier = modifier
        .surface(
          shape = shape,
          backgroundColor = color,
          borderColor = border,
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
  border: Color? = null,
  indication: Indication? = null,
  interactionSource: MutableInteractionSource? = null,
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(LocalContentColor provides contentColor) {
    Box(
      modifier = modifier
        .surface(
          shape = shape,
          backgroundColor = color,
          borderColor = border,
        )
        .clickable(
          interactionSource = interactionSource,
          indication = indication ?: LocalIndication.current,
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
private fun Modifier.surface(shape: Shape, backgroundColor: Color, borderColor: Color?) = this
  .then(
    if (borderColor != null) {
      Modifier.drawWithCache {
        val stroke = Stroke(1.dp.toPx())
        val outline = shape.createOutline(size.copy(size.width, size.height), layoutDirection, this)
        onDrawWithContent {
          drawContent()
          drawOutline(outline, borderColor, style = stroke)
        }
      }
    } else {
      Modifier
    },
  )
  .background(color = backgroundColor, shape = shape)
  .clip(shape)
