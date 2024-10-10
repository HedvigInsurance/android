package com.hedvig.android.core.ui.appbar.m3

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.Modifier
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.X
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.icon.ArrowLeft
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons

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
        style = HedvigTheme.typography.headlineSmall,
      )
    },
    navigationIcon = {
      IconButton(
        onClick = { onClick() },
        content = {
          Icon(
            imageVector = when (actionType) {
              TopAppBarActionType.BACK -> HedvigIcons.ArrowLeft
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
