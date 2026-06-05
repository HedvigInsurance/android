package com.hedvig.android.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@ContributesBinding(AppScope::class, replaces = [NoopPermissionManager::class])
@SingleIn(AppScope::class)
@Inject
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
