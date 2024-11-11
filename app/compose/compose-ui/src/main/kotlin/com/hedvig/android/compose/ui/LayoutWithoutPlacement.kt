package com.hedvig.android.compose.ui

import android.text.Layout
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout

/**
 * A [Layout] which places [sizeAdjustingContent] first in a box, making that box take *at least* its size but does not
 * draw [sizeAdjustingContent] on the screen, and then puts [content], making sure that it takes up *at least* as much
 * size as [sizeAdjustingContent] would take.
 */
@Composable
fun LayoutWithoutPlacement(
  modifier: Modifier = Modifier,
  sizeAdjustingContent: @Composable () -> Unit,
  content: @Composable () -> Unit,
) {
  Layout(
    modifier = modifier,
    content = {
      Box(propagateMinConstraints = true) { sizeAdjustingContent() }
      Box(propagateMinConstraints = true) { content() }
    },
  ) { measurable, constraints ->
    val sizeAdjustingContentPlaceable = measurable[0].measure(constraints)
    val contentPlaceable = measurable[1].measure(
      constraints.copy(
        minWidth = sizeAdjustingContentPlaceable.width.coerceAtLeast(constraints.minWidth),
        minHeight = sizeAdjustingContentPlaceable.height.coerceAtLeast(constraints.minHeight),
      ),
    )
    layout(contentPlaceable.width, contentPlaceable.height) {
      contentPlaceable.place(0, 0)
    }
  }
}

fun Modifier.withoutPlacement(): Modifier = this.layout { measurable, constraints ->
  val boxPlaceable = measurable.measure(constraints)
  layout(width = boxPlaceable.width, height = boxPlaceable.height) {}
}
