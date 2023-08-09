package com.hedvig.android.core.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hedvig.android.core.designsystem.material3.squircleMedium
import hedvig.resources.R

@Composable
fun HedvigAlertDialog(
  title: String?,
  text: String,
  onDismissRequest: () -> Unit,
  onConfirmClick: () -> Unit,
  modifier: Modifier = Modifier,
  confirmButtonLabel: String = stringResource(R.string.GENERAL_YES),
  dismissButtonLabel: String = stringResource(R.string.GENERAL_NO),
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
    shape = MaterialTheme.shapes.squircleMedium,
    dismissButton = {
      TextButton(
        shape = MaterialTheme.shapes.squircleMedium,
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
        shape = MaterialTheme.shapes.squircleMedium,
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
