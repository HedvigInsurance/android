package com.hedvig.feature.claim.chat.ui.audiorecording

import androidx.compose.runtime.Composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus as AccompanistPermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState as accompanistRememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun rememberPermissionState(permission: String, onPermissionResult: (Boolean) -> Unit): PermissionState {
  val accompanistState = accompanistRememberPermissionState(permission, onPermissionResult)

  return object : PermissionState {
    override val permission: String = accompanistState.permission

    override val status: PermissionStatus
      get() = when {
        accompanistState.status.isGranted -> PermissionStatus.Granted
        else -> PermissionStatus.Denied(
          shouldShowRationale = (accompanistState.status as? AccompanistPermissionStatus.Denied)
            ?.shouldShowRationale ?: false,
        )
      }

    override fun launchPermissionRequest() {
      accompanistState.launchPermissionRequest()
    }
  }
}
