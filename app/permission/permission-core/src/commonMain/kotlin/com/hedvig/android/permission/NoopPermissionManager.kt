package com.hedvig.android.permission

import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class NoopPermissionManager : PermissionManager {
  override fun isPermissionGranted(permission: Permission): Boolean = false
}
