package com.hedvig.android.odyssey.input.ui.audiorecorder

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionDialog(
  recordAudioPermissionState: PermissionState,
  permissionTitle: String,
  permissionMessage: String,
  dismiss: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = dismiss,
    title = { Text(permissionTitle) },
    text = { Text(permissionMessage) },
    dismissButton = {
      TextButton(
        onClick = dismiss,
      ) {
        Text(stringResource(android.R.string.cancel))
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          dismiss()
          recordAudioPermissionState.launchPermissionRequest()
        },
      ) {
        Text(stringResource(android.R.string.ok))
      }
    },
  )
}
