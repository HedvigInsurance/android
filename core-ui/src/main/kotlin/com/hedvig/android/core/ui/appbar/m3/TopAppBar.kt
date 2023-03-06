package com.hedvig.android.core.ui.appbar.m3

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
    windowInsets = windowInsets,
    colors = colors,
    scrollBehavior = scrollBehavior,
    modifier = modifier,
  )
}

@Suppress("unused")
@Composable
fun TopAppBarWithClose(
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
    actionType = TopAppBarActionType.CLOSE,
    windowInsets = windowInsets,
    colors = colors,
    scrollBehavior = scrollBehavior,
    modifier = modifier,
  )
}

private enum class TopAppBarActionType {
  BACK, CLOSE
}

@Composable
private inline fun TopAppBar(
  title: String,
  crossinline onClick: () -> Unit,
  actionType: TopAppBarActionType,
  windowInsets: WindowInsets,
  colors: TopAppBarColors,
  scrollBehavior: TopAppBarScrollBehavior,
  modifier: Modifier = Modifier,
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
              TopAppBarActionType.BACK -> Icons.Filled.ArrowBack
              TopAppBarActionType.CLOSE -> Icons.Filled.Close
            },
            contentDescription = null,
          )
        },
      )
    },
    windowInsets = windowInsets,
    colors = colors,
    scrollBehavior = scrollBehavior,
  )
}
