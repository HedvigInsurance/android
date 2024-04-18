package com.hedvig.android.core.ui.appbar.m3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.ArrowBack
import com.hedvig.android.core.icons.hedvig.normal.X

/**
 * Just the layout and placing of the top app bar, without the background, drag handling and so on. Can be used to
 * simply place the actions in the right spot, without interfering in other ways like swallowing the touch events on it.
 */
@Composable
fun TopAppBarLayoutForActions(modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {}) {
  Row(
    horizontalArrangement = Arrangement.End,
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .windowInsetsPadding(
        WindowInsets.systemBars
          .union(WindowInsets.displayCutout)
          .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
      )
      .height(64.dp)
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  ) {
    actions()
  }
}

@Composable
fun TopAppBarWithBack(
  title: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
  colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.background,
    scrolledContainerColor = MaterialTheme.colorScheme.surface,
  ),
  scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
) {
  TopAppBar(
    title = title,
    onClick = onClick,
    actionType = TopAppBarActionType.BACK,
    colors = colors,
    scrollBehavior = scrollBehavior,
    modifier = modifier,
    windowInsets = windowInsets,
  )
}

enum class TopAppBarActionType {
  BACK,
  CLOSE,
}

@Composable
fun TopAppBar(
  title: String,
  onClick: () -> Unit,
  actionType: TopAppBarActionType,
  colors: TopAppBarColors,
  scrollBehavior: TopAppBarScrollBehavior,
  modifier: Modifier = Modifier,
  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
  actions: @Composable RowScope.() -> Unit = {},
) {
  TopAppBar(
    modifier = modifier,
    title = {
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
      )
    },
    navigationIcon = {
      IconButton(
        onClick = { onClick() },
        content = {
          Icon(
            imageVector = when (actionType) {
              TopAppBarActionType.BACK -> Icons.Hedvig.ArrowBack
              TopAppBarActionType.CLOSE -> Icons.Hedvig.X
            },
            contentDescription = null,
          )
        },
      )
    },
    windowInsets = windowInsets,
    colors = colors,
    scrollBehavior = scrollBehavior,
    actions = actions,
  )
}
