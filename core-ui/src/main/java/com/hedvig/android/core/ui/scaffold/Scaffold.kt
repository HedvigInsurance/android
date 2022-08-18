package com.hedvig.android.core.ui.scaffold

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy

/**
 * Stripped version of [androidx.compose.material.Scaffold] which only contains the bottomBar slot.
 */
@Composable
fun Scaffold(
  bottomBar: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  backgroundColor: Color = MaterialTheme.colors.background,
  contentColor: Color = contentColorFor(backgroundColor),
  content: @Composable (PaddingValues) -> Unit,
) {
  Surface(modifier = modifier, color = backgroundColor, contentColor = contentColor) {
    ScaffoldLayout(
      content = content,
      bottomAnchoredContent = bottomBar,
    )
  }
}

@Composable
@UiComposable
private fun ScaffoldLayout(
  content:
    @Composable
    @UiComposable
    (PaddingValues) -> Unit,
  bottomAnchoredContent:
    @Composable
    @UiComposable
    () -> Unit,
) {
  SubcomposeLayout { constraints ->
    val layoutWidth = constraints.maxWidth
    val layoutHeight = constraints.maxHeight

    val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

    layout(layoutWidth, layoutHeight) {
      val bottomAnchoredPlaceables = subcompose(ScaffoldLayoutContent.BottomBar, bottomAnchoredContent).fastMap {
        it.measure(looseConstraints)
      }

      val bottomBarHeight = bottomAnchoredPlaceables.fastMaxBy { it.height }?.height ?: 0

      val bodyContentPlaceables = subcompose(ScaffoldLayoutContent.MainContent) {
        val innerPadding = PaddingValues(bottom = bottomBarHeight.toDp())
        content(innerPadding)
      }.map { it.measure(looseConstraints.copy(maxHeight = layoutHeight)) }

      bodyContentPlaceables.forEach {
        it.place(0, 0)
      }
      bottomAnchoredPlaceables.forEach {
        it.place(0, layoutHeight - bottomBarHeight)
      }
    }
  }
}

private enum class ScaffoldLayoutContent { MainContent, BottomBar }
