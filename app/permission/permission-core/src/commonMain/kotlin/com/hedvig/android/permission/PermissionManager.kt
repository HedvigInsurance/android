package com.hedvig.android.permission

interface PermissionManager {
  fun isPermissionGranted(permission: Permission): Boolean
}
