package com.hedvig.android.core.ui.appbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.ui.TopAppBar

@Composable
fun TopAppBarWithBack(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  title: String? = null,
  backgroundColor: Color = MaterialTheme.colorScheme.background,
  contentPadding: PaddingValues = PaddingValues(0.dp),
) {
  TopAppBar(
    onClick,
    TopAppBarActionType.BACK,
    modifier,
    title,
    backgroundColor,
    contentPadding,
  )
}

@Composable
fun TopAppBarWithClose(
  onClick: () -> Unit,
  title: String,
  modifier: Modifier = Modifier,
  backgroundColor: Color = MaterialTheme.colorScheme.background,
  contentPadding: PaddingValues = PaddingValues(0.dp),
) {
  TopAppBar(
    onClick,
    TopAppBarActionType.CLOSE,
    modifier,
    title,
    backgroundColor,
    contentPadding,
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
  backgroundColor: Color = MaterialTheme.colorScheme.background,
  contentPadding: PaddingValues = PaddingValues(0.dp),
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
    contentPadding = contentPadding,
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
    backgroundColor = backgroundColor,
    elevation = 0.dp,
  )
}
