package com.hedvig.app.ui.compose.composables

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hedvig.app.R

@Composable
fun ErrorDialog(
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.error_dialog_title),
    message: String?
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
                Text(stringResource(R.string.error_dialog_button))
            }
        }
    )
}
