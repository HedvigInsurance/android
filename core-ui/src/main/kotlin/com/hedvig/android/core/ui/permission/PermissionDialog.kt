package com.hedvig.android.core.ui.permission

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/**
 * [permissionDescription] The text which will be used as the description of why we want this permission
 * [isPermanentlyDeclined] Fetched by querying [android.app.Activity.shouldShowRequestPermissionRationale]. We can rely
 *   on this only when we've already received a negative response from the rememberNotificationPermissionState callback
 * [onDismiss] Used to dismiss the dialog itself
 * [okClick] The callback when we know we can ask for the permission. Should be permissionState::launchPermissionRequest
 * [openAppSettings] This gets triggered when we know we've been declined the permission, to open the app settings
 *
 *  Sample usage:
 *  ```
 *  var showDialog by remember { mutableStateOf(false) }
 *  val permissionState: PermissionState = rememberPermissionState(Manifest.permission.FOO) { isGranted ->
 *    if (isGranted) {
 *      doActionWithPermissionGranted()
 *    } else {
 *      showDialog = true
 *    }
 *  }
 *  if (showDialog) {
 *    PermissionDialog(
 *      permissionDescription = stringResource(R.string.PERMISSION_DIALOG_RECORD_AUDIO_MESSAGE),
 *      isPermanentlyDeclined = !shouldShowRequestPermissionRationale(Manifest.permission.FOO),
 *      onDismiss = { showDialog = false },
 *      okClick = permissionState::launchPermissionRequest,
 *      openAppSettings = openAppSettings,
 *    )
 *  }
 *  // and then to use it
 *  SomeComposable(
 *    // This will ask for permission and then do the action or do the action directly when the permission is already given
 *    onClick = permissionState.launchPermissionRequest()
 *  )
 *  ```
 */
@Composable
fun PermissionDialog(
  permissionDescription: String,
  isPermanentlyDeclined: Boolean,
  onDismiss: () -> Unit,
  okClick: () -> Unit,
  openAppSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        shape = MaterialTheme.shapes.medium,
      ) {
        Text(stringResource(android.R.string.cancel))
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          onDismiss()
          if (isPermanentlyDeclined) {
            openAppSettings()
          } else {
            okClick()
          }
        },
        shape = MaterialTheme.shapes.medium,
      ) {
        Text(
          if (isPermanentlyDeclined) {
            stringResource(hedvig.resources.R.string.profile_appSettingsSection_title)
          } else {
            stringResource(android.R.string.ok)
          },
        )
      }
    },
    title = { Text(stringResource(hedvig.resources.R.string.PERMISSION_DIALOG_TITLE)) },
    text = { Text(permissionDescription) },
    modifier = modifier,
  )
}
