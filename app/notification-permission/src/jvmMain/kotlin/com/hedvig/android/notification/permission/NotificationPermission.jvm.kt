package com.hedvig.android.notification.permission

import androidx.compose.runtime.Composable

/**
 * The JVM target has no notification permission to request, so the state always reads as denied and never shows a
 * dialog.
 */
private object UnavailableNotificationPermissionState : NotificationPermissionState {
  override val permission: String = "android.permission.POST_NOTIFICATIONS"
  override val status: PermissionStatus = PermissionStatus.Denied(shouldShowRationale = false)
  override val showDialog: Boolean = false

  override fun launchPermissionRequest() {}

  override fun dismissDialog() {}
}

@Composable
actual fun rememberNotificationPermissionState(onNotificationGranted: () -> Unit): NotificationPermissionState {
  return UnavailableNotificationPermissionState
}

@Composable
actual fun rememberNotificationPermissionStatus(): Boolean = false
