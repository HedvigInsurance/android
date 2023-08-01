package com.hedvig.android.core.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hedvig.android.core.designsystem.material3.squircle
import hedvig.resources.R

@Composable
fun HedvigAlertDialog(
  modifier: Modifier = Modifier,
  title: String?,
  text: String,
  confirmButtonLabel: String = stringResource(R.string.GENERAL_YES),
  dismissButtonLabel: String = stringResource(R.string.GENERAL_NO),
  onDismissRequest: () -> Unit,
  onConfirmClick: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismissRequest,
    title = {
      if (title != null) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge,
        )
      }
    },
    text = {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
      )
    },
    shape = MaterialTheme.shapes.squircle,
    dismissButton = {
      TextButton(
        shape = MaterialTheme.shapes.squircle,
        onClick = onDismissRequest,
      ) {
        Text(
          text = dismissButtonLabel,
          style = MaterialTheme.typography.bodyLarge,
        )
      }
    },
    confirmButton = {
      TextButton(
        shape = MaterialTheme.shapes.squircle,
        onClick = {
          onDismissRequest()
          onConfirmClick()
        },
      ) {
        Text(
          text = confirmButtonLabel,
          style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.error),
        )
      }
    },
    modifier = modifier,
  )
}
