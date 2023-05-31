package com.hedvig.android.core.ui.scaffold

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import com.hedvig.android.core.ui.appbar.m3.TopAppBar
import com.hedvig.android.core.ui.appbar.m3.TopAppBarActionType

/**
 * A custom scaffold, with a built-in top app bar which is pinned to the top and changes colors according to
 * [topAppBarColors] on scroll.
 * Handles all insets by default, leaving the [content] to take place in the available screen.
 * Gives a [ColumnScope] to the content, and makes the content scrollable by default.
 */
@Composable
fun HedvigScaffold(
  navigateUp: () -> Unit,
  modifier: Modifier = Modifier,
  topAppBarText: String? = null,
  topAppBarActionType: TopAppBarActionType = TopAppBarActionType.BACK,
  itemsColumnHorizontalAlignment: Alignment.Horizontal = Alignment.Start,
  topAppBarColors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
    scrolledContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
  ),
  topAppBarScrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
  scrollState: ScrollState = rememberScrollState(),
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(modifier.fillMaxSize()) {
    TopAppBar(
      onClick = navigateUp,
      title = topAppBarText ?: "",
      colors = topAppBarColors,
      scrollBehavior = topAppBarScrollBehavior,
      actionType = topAppBarActionType,
    )
    Column(
      horizontalAlignment = itemsColumnHorizontalAlignment,
      modifier = Modifier
        .fillMaxSize()
        .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
        .verticalScroll(scrollState)
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
      content()
      Spacer(Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)))
    }
  }
}

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
  content: @Composable @UiComposable (PaddingValues) -> Unit, // ktlint-disable annotation
  bottomAnchoredContent: @Composable @UiComposable () -> Unit, // ktlint-disable annotation
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
