package com.hedvig.android.permission

import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/** The desktop app holds none of the [Permission]s, so every one reads as not granted. */
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class JvmPermissionManager : PermissionManager {
  override fun isPermissionGranted(permission: Permission): Boolean = false
}
