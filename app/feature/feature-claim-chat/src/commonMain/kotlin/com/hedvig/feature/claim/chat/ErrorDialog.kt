package com.hedvig.feature.claim.chat

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ErrorDialog(
  message: String,
  onDismiss: () -> Unit, // Function to call when the user clicks "OK"
) {
  AlertDialog(
    onDismissRequest = onDismiss, // Dismiss when clicking outside
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("OK")
      }
    },
    title = { Text("Error") },
    text = { Text(message) },
  )
}
