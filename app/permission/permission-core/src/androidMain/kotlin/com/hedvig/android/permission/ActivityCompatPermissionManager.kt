package com.hedvig.android.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

internal class ActivityCompatPermissionManager(
  private val context: Context,
) : PermissionManager {
  override fun isPermissionGranted(permission: Permission): Boolean {
    return ActivityCompat.checkSelfPermission(context, permission.toAndroidPermission()) ==
      PackageManager.PERMISSION_GRANTED
  }

  private fun Permission.toAndroidPermission(): String = when (this) {
    Permission.PostNotifications -> "android.permission.POST_NOTIFICATIONS"
  }
}
