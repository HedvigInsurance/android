package com.hedvig.android.notification.permission

import androidx.compose.runtime.Composable

/**
 * The JVM target has no notification permission to ask for, so the state reads as granted and never shows a dialog:
 * nothing prompts for the permission or reminds the member to enable notifications.
 */
private object UnavailableNotificationPermissionState : NotificationPermissionState {
  override val permission: String = "android.permission.POST_NOTIFICATIONS"
  override val status: PermissionStatus = PermissionStatus.Granted
  override val showDialog: Boolean = false

  override fun launchPermissionRequest() {}

  override fun dismissDialog() {}
}

@Composable
actual fun rememberNotificationPermissionState(onNotificationGranted: () -> Unit): NotificationPermissionState {
  return UnavailableNotificationPermissionState
}

@Composable
actual fun rememberNotificationPermissionStatus(): Boolean = true
