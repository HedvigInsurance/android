package com.hedvig.android.core.ui.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout

/**
 * A [Layout] which places [sizeAdjustingContent] first in a box, making that box take *at least* its size but does not
 * draw [sizeAdjustingContent] on the screen, and then puts [content]
 */
@Composable
fun LayoutWithoutPlacement(
  modifier: Modifier = Modifier,
  sizeAdjustingContent: @Composable BoxScope.() -> Unit,
  content: @Composable BoxScope.() -> Unit,
) {
  Box(modifier, Alignment.Center) {
    Box(Modifier.withoutPlacement()) { sizeAdjustingContent() }
    content()
  }
}

fun Modifier.withoutPlacement(): Modifier = this.layout { measurable, constraints ->
  val boxPlaceable = measurable.measure(constraints)
  layout(width = boxPlaceable.width, height = boxPlaceable.height) {}
}
