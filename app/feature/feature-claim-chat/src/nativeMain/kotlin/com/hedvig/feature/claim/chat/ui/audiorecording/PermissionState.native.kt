package com.hedvig.feature.claim.chat.ui.audiorecording

import androidx.compose.runtime.Composable

@Composable
actual fun rememberPermissionState(
  permission: String,
  onPermissionResult: (Boolean) -> Unit,
): PermissionState {
  // TODO ios: Implement iOS permission handling
  return object : PermissionState {
    override val permission: String = permission
    override val status: PermissionStatus = PermissionStatus.Granted
    override fun launchPermissionRequest() {
      onPermissionResult(true)
    }
  }
}
