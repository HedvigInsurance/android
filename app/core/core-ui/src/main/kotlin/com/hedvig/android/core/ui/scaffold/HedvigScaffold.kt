package com.hedvig.android.core.ui.scaffold

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
  color: Color = MaterialTheme.colorScheme.background,
  topAppBarText: String? = null,
  topAppBarActionType: TopAppBarActionType = TopAppBarActionType.BACK,
  itemsColumnHorizontalAlignment: Alignment.Horizontal = Alignment.Start,
  topAppBarColors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.background,
    scrolledContainerColor = MaterialTheme.colorScheme.surface,
  ),
  topAppBarScrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
  scrollState: ScrollState = rememberScrollState(),
  topAppBarActions: @Composable RowScope.() -> Unit = {},
  content: @Composable ColumnScope.() -> Unit,
) {
  Surface(color = color, modifier = modifier.fillMaxSize()) {
    Column {
      TopAppBar(
        title = topAppBarText ?: "",
        onClick = navigateUp,
        actionType = topAppBarActionType,
        colors = topAppBarColors,
        scrollBehavior = topAppBarScrollBehavior,
        actions = topAppBarActions,
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
}
