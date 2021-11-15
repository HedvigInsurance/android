package com.hedvig.app.ui.compose.composables

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.hedvig.app.R

@Composable
fun ErrorDialog(
    show: MutableState<Boolean>,
    title: String = "Error",
    message: String?
) {
    AlertDialog(
        onDismissRequest = {
            show.value = false
        },
        title = {
            Text(title)
        },
        text = {
            Text(message ?: stringResource(id = R.string.general_unknown_error))
        },
        confirmButton = {
            TextButton(onClick = { show.value = false }) {
                Text("Close")
            }
        }
    )
}
