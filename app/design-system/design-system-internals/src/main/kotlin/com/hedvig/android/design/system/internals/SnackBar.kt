package com.hedvig.android.design.system.internals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult.ActionPerformed
import androidx.compose.material3.SnackbarResult.Dismissed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun InternalSnackBar(
  snackbarText: String,
  showSnackbar: Boolean,
  showedSnackbar: () -> Unit,
  colors: NotificationColors,
  shape: Shape,
  textStyle: TextStyle,
  icon: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  action: (() -> Unit)? = null,
  actionLabel: String? = null,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  LaunchedEffect(showSnackbar, snackbarText) {
    if (!showSnackbar) return@LaunchedEffect
    val result = snackbarHostState.showSnackbar(snackbarText, actionLabel = actionLabel)
    when (result) {
      Dismissed -> {
        showedSnackbar()
      }

      ActionPerformed -> {
        if (action != null) {
          action()
        }
        showedSnackbar()
      }
    }
  }
  SnackbarHost(snackbarHostState, modifier.padding(16.dp)) { _ ->
    Snackbar(
      action = {
        if (actionLabel != null) {
          ActionContent(
            actionLabel,
            textStyle,
            modifier = Modifier.clip(shape).clickable(enabled = true, onClick = action ?: showedSnackbar).padding(
              top = 16.dp,
              bottom = 16.dp,
              start = 8.dp,
              end = 8.dp,
            ),
          )
        }
      },
      dismissAction = null,
      actionOnNewLine = false,
      shape = shape,
      containerColor = colors.containerColor,
      contentColor = colors.textColor,
      actionContentColor = colors.textColor,
    ) {
      if (actionLabel != null) {
        SnackContentForAction(
          icon = icon,
          snackbarText = snackbarText,
          textStyle = textStyle,
          modifier = Modifier.padding(
            top = 16.dp,
            bottom = 16.dp,
            end = 8.dp,
          ),
        )
      } else {
        PlainSnackContent(
          icon = icon,
          snackbarText = snackbarText,
          textStyle = textStyle,
          modifier = Modifier
            .padding(
              top = 16.dp,
              bottom = 16.dp,
            )
            .fillMaxWidth(),
        )
      }
    }
  }
}

@Composable
private fun ActionContent(actionLabel: String, textStyle: TextStyle, modifier: Modifier) {
  Text(
    actionLabel,
    style = textStyle.copy(textDecoration = TextDecoration.Underline),
    modifier = modifier,
  )
}

@Composable
private fun PlainSnackContent(
  icon: @Composable () -> Unit,
  snackbarText: String,
  textStyle: TextStyle,
  modifier: Modifier,
) {
  Row(
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier,
  ) {
    icon()
    Spacer(Modifier.width(8.dp))
    Text(snackbarText, style = textStyle)
  }
}

@Composable
private fun SnackContentForAction(
  icon: @Composable () -> Unit,
  snackbarText: String,
  textStyle: TextStyle,
  modifier: Modifier,
) {
  Row(
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier,
  ) {
    icon()
    Spacer(Modifier.width(8.dp))
    Text(snackbarText, style = textStyle)
  }
}

data class NotificationColors(
  val containerColor: Color,
  val textColor: Color,
  val iconColor: Color,
)
