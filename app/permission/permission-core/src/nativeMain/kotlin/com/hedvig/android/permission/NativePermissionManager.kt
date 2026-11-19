package com.hedvig.android.permission

import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/** The iOS app checks its permissions in Swift, so from Kotlin every [Permission] reads as not granted. */
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class NativePermissionManager : PermissionManager {
  override fun isPermissionGranted(permission: Permission): Boolean = false
}
