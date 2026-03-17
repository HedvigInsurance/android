package com.hedvig.android.permission

internal class NoopPermissionManager : PermissionManager {
  override fun isPermissionGranted(permission: Permission): Boolean = false
}
