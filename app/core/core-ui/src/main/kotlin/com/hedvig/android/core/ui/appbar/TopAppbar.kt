package com.hedvig.android.core.ui.appbar

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.ArrowBack
import com.hedvig.android.core.icons.hedvig.normal.X

@Composable
fun TopAppBarWithBack(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  title: String? = null,
  containerColor: Color = MaterialTheme.colorScheme.background,
  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
  TopAppBar(
    onClick,
    TopAppBarActionType.BACK,
    modifier,
    title,
    containerColor,
    windowInsets,
  )
}

private enum class TopAppBarActionType {
  BACK,
  CLOSE,
}

@Composable
private inline fun TopAppBar(
  crossinline onClick: () -> Unit,
  actionType: TopAppBarActionType,
  modifier: Modifier = Modifier,
  title: String? = null,
  containerColor: Color = MaterialTheme.colorScheme.background,
  windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
  TopAppBar(
    modifier = modifier,
    title = {
      if (title != null) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge,
        )
      }
    },
    windowInsets = windowInsets,
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
    colors = TopAppBarDefaults.topAppBarColors(containerColor = containerColor),
  )
}
