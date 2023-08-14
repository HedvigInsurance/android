package com.hedvig.android.core.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import com.hedvig.android.core.designsystem.material3.squircleMedium
import hedvig.resources.R

@Composable
fun ErrorDialog(
  message: String?,
  onDismiss: () -> Unit,
  title: String = stringResource(R.string.general_unknown_error),
  shape: Shape = MaterialTheme.shapes.squircleMedium,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(title)
    },
    text = {
      Text(message ?: stringResource(id = R.string.general_unknown_error))
    },
    shape = shape,
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.general_close_button))
      }
    },
  )
}
