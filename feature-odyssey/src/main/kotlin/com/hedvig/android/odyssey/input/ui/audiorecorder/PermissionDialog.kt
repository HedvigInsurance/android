package com.hedvig.android.odyssey.input.ui.audiorecorder

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionDialog(
  recordAudioPermissionState: PermissionState,
  openDialog: MutableState<Boolean>,
  permissionTitle: String,
  permissionMessage: String,
) {
  AlertDialog(
    onDismissRequest = { openDialog.value = false },
    title = { Text(permissionTitle) },
    text = { Text(permissionMessage) },
    dismissButton = {
      TextButton(
        onClick = { openDialog.value = false },
      ) {
        Text(stringResource(android.R.string.cancel))
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          openDialog.value = false
          recordAudioPermissionState.launchPermissionRequest()
        },
      ) {
        Text(stringResource(android.R.string.ok))
      }
    },
  )
}
