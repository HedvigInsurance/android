package com.hedvig.feature.claim.chat.ui.audiorecording

import androidx.compose.runtime.Composable
import com.hedvig.feature.claim.chat.ui.step.audiorecording.PermissionState
import com.hedvig.feature.claim.chat.ui.step.audiorecording.PermissionStatus

@Composable
actual fun rememberPermissionState(permission: String, onPermissionResult: (Boolean) -> Unit): PermissionState {
  return object : PermissionState {
    override val permission: String = permission
    override val status: PermissionStatus = PermissionStatus.Granted

    override fun launchPermissionRequest() {
      onPermissionResult(true)
    }
  }
}
