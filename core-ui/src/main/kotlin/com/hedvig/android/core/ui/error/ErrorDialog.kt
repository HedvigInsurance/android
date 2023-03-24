package com.hedvig.app.ui.compose.composables

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import hedvig.resources.R

@Composable
fun ErrorDialog(
  message: String?,
  onDismiss: () -> Unit,
  title: String = stringResource(R.string.general_unknown_error),
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(title)
    },
    text = {
      Text(message ?: stringResource(id = R.string.general_unknown_error))
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.general_close_button))
      }
    },
  )
}
