package com.hedvig.feature.claim.chat.ui.audiorecording

import androidx.compose.runtime.Composable

/**
 * Represents the state of a permission in a multiplatform way
 */
interface PermissionState {
  val permission: String
  val status: PermissionStatus

  fun launchPermissionRequest()
}

sealed interface PermissionStatus {
  data object Granted : PermissionStatus

  data class Denied(val shouldShowRationale: Boolean) : PermissionStatus
}

/**
 * Remember a permission state for the given permission
 */
@Composable
expect fun rememberPermissionState(permission: String, onPermissionResult: (Boolean) -> Unit = {}): PermissionState
