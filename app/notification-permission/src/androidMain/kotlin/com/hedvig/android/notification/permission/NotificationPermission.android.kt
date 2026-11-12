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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MutablePermissionState
import com.google.accompanist.permissions.PermissionLifecycleCheckerEffect
import com.google.accompanist.permissions.PermissionState as AccompanistPermissionState
import com.google.accompanist.permissions.PermissionStatus as AccompanistPermissionStatus
import com.google.accompanist.permissions.isGranted as accompanistIsGranted
import com.google.accompanist.permissions.rememberPermissionState

private class NotificationPermissionStateImpl(
  private val permissionState: AccompanistPermissionState,
) : NotificationPermissionState {
  private var _showDialog by mutableStateOf(false)

  override val permission: String
    get() = permissionState.permission

  override val status: PermissionStatus
    get() = when (val status = permissionState.status) {
      AccompanistPermissionStatus.Granted -> PermissionStatus.Granted
      is AccompanistPermissionStatus.Denied -> PermissionStatus.Denied(status.shouldShowRationale)
    }

  override val showDialog: Boolean
    get() = _showDialog

  override fun launchPermissionRequest() {
    permissionState.launchPermissionRequest()
  }

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
actual fun rememberNotificationPermissionState(onNotificationGranted: () -> Unit): NotificationPermissionState {
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

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun rememberNotificationPermissionStatus(): Boolean {
  val isPreview = LocalInspectionMode.current
  return if (!isPreview && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS).status.accompanistIsGranted
  } else {
    true
  }
}
