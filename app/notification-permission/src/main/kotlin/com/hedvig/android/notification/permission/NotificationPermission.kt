@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.hedvig.android.notification.permission

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MutablePermissionState
import com.google.accompanist.permissions.PermissionLifecycleCheckerEffect
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.hedvig.android.design.system.hedvig.PermissionDialog
import hedvig.resources.R

interface NotificationPermissionState : PermissionState {
  val showDialog: Boolean

  fun dismissDialog()
}

private class NotificationPermissionStateImpl(
  private val permissionState: PermissionState,
) : NotificationPermissionState, PermissionState by permissionState {
  private var _showDialog by mutableStateOf(false)

  override val showDialog: Boolean
    get() = _showDialog

  override fun dismissDialog() {
    _showDialog = false
  }

  fun showDialog() {
    _showDialog = true
  }
}

/**
 * Convenience function over [com.google.accompanist.permissions.rememberPermissionState] specifically for the
 * notification. Use in conjuction with [NotificationPermissionDialog] to get an easy to use notification permission
 * handling.
 */
@SuppressLint("InlinedApi")
@Composable
fun rememberNotificationPermissionState(onNotificationGranted: () -> Unit = {}): NotificationPermissionState {
  val context = LocalContext.current
  val activity = LocalActivity.current
  val permissionState = remember {
    MutablePermissionState(Manifest.permission.POST_NOTIFICATIONS, context, activity)
  }
  val notificationPermissionState = remember {
    NotificationPermissionStateImpl(permissionState)
  }
  PermissionLifecycleCheckerEffect(permissionState)
  val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
    permissionState.refreshPermissionStatus()
    if (isGranted) {
      onNotificationGranted()
    } else {
      notificationPermissionState.showDialog()
    }
  }
  DisposableEffect(permissionState, launcher) {
    permissionState.launcher = launcher
    onDispose {
      permissionState.launcher = null
    }
  }

  return notificationPermissionState
}

@Composable
fun NotificationPermissionDialog(
  notificationPermissionState: NotificationPermissionState,
  openAppSettings: () -> Unit,
) {
  if (notificationPermissionState.showDialog) {
    PermissionDialog(
      permissionDescription = stringResource(R.string.CLAIMS_ACTIVATE_NOTIFICATIONS_BODY),
      isPermanentlyDeclined = !notificationPermissionState.status.shouldShowRationale,
      onDismiss = notificationPermissionState::dismissDialog,
      okClick = notificationPermissionState::launchPermissionRequest,
      openAppSettings = openAppSettings,
    )
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberNotificationPermissionStatus(): Boolean {
  val isPreview = LocalInspectionMode.current
  return if (!isPreview && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS).status.isGranted
  } else {
    true
  }
}

fun rememberPreviewNotificationPermissionState(
  permissionStatus: PermissionStatus = PermissionStatus.Granted,
  isDialogShowing: Boolean = false,
): NotificationPermissionState = object : NotificationPermissionState {
  override val showDialog: Boolean = isDialogShowing

  override fun dismissDialog() {}

  override val permission: String = ""
  override val status: PermissionStatus = permissionStatus

  override fun launchPermissionRequest() {}
}
