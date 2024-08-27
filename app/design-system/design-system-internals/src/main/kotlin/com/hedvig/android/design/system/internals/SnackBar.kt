package com.hedvig.android.design.system.internals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
  val contentModifier = if (actionLabel != null)
    Modifier.padding(paddingForContentWithAction)
  else
    Modifier
    .padding(paddingForContentWithoutAction)
    .fillMaxWidth()
  val contentHorizontalArrangement = if (actionLabel != null) Arrangement.Start else Arrangement.Center
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
  SnackbarHost(snackbarHostState, modifier) { _ ->
    Snackbar(
      action =
      if (actionLabel == null) null else {
        {
          ActionContent(
            actionLabel,
            textStyle,
            modifier = Modifier
              .clip(shape)
              .clickable(enabled = true, onClick = action ?: showedSnackbar)
              .padding(actionPadding),
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
      SnackContent(
        icon = icon,
        snackbarText = snackbarText,
        textStyle = textStyle,
        modifier = contentModifier,
        horizontalArrangement = contentHorizontalArrangement,
      )
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
private fun SnackContent(
  icon: @Composable () -> Unit,
  snackbarText: String,
  textStyle: TextStyle,
  horizontalArrangement: Arrangement.Horizontal,
  modifier: Modifier,
) {
  Row(
    horizontalArrangement = horizontalArrangement,
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

private val actionPadding = PaddingValues(
  top = 16.dp,
  bottom = 16.dp,
  start = 8.dp,
  end = 8.dp,
)

private val paddingForContentWithAction = PaddingValues(
  top = 16.dp,
  bottom = 16.dp,
  end = 8.dp,
)
private val paddingForContentWithoutAction = PaddingValues(
  top = 16.dp,
  bottom = 16.dp,
)

