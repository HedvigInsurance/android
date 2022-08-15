package com.hedvig.app.ui.compose.composables

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hedvig.app.R

@Composable
fun ErrorDialog(
  message: String?,
  onDismiss: () -> Unit,
  title: String = stringResource(com.adyen.checkout.dropin.R.string.error_dialog_title),
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(title)
    },
    text = {
      Text(message ?: stringResource(id = hedvig.resources.R.string.general_unknown_error))
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(com.adyen.checkout.dropin.R.string.error_dialog_button))
      }
    },
  )
}
