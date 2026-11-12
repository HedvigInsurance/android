package com.hedvig.android.notification.permission

import androidx.compose.runtime.Composable
import com.hedvig.android.design.system.hedvig.PermissionDialog
import hedvig.resources.CLAIMS_ACTIVATE_NOTIFICATIONS_BODY
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

interface NotificationPermissionState {
  val permission: String
  val status: PermissionStatus
  val showDialog: Boolean

  fun launchPermissionRequest()

  fun dismissDialog()
}

sealed interface PermissionStatus {
  data object Granted : PermissionStatus

  data class Denied(val shouldShowRationale: Boolean) : PermissionStatus
}

val PermissionStatus.isGranted: Boolean
  get() = this == PermissionStatus.Granted

val PermissionStatus.shouldShowRationale: Boolean
  get() = when (this) {
    PermissionStatus.Granted -> false
    is PermissionStatus.Denied -> shouldShowRationale
  }

/**
 * Notification permission state for the current screen. [onNotificationGranted] runs when a permission request is
 * granted; a denied request makes [NotificationPermissionState.showDialog] true. Use in conjunction with
 * [NotificationPermissionDialog].
 */
@Composable
expect fun rememberNotificationPermissionState(onNotificationGranted: () -> Unit = {}): NotificationPermissionState

@Composable
fun NotificationPermissionDialog(
  notificationPermissionState: NotificationPermissionState,
  openAppSettings: () -> Unit,
) {
  if (notificationPermissionState.showDialog) {
    PermissionDialog(
      permissionDescription = stringResource(Res.string.CLAIMS_ACTIVATE_NOTIFICATIONS_BODY),
      isPermanentlyDeclined = !notificationPermissionState.status.shouldShowRationale,
      onDismiss = notificationPermissionState::dismissDialog,
      okClick = notificationPermissionState::launchPermissionRequest,
      openAppSettings = openAppSettings,
    )
  }
}

@Composable
expect fun rememberNotificationPermissionStatus(): Boolean

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
