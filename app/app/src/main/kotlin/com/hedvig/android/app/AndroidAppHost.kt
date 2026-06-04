package com.hedvig.android.app

import androidx.activity.SystemBarStyle

/**
 * The Activity-bound capabilities the Compose app shell needs from its host. Implemented by
 * [MainActivity] and handed to the shell as a single dependency instead of four loose callbacks.
 * Mirrors the [com.hedvig.android.navigation.activity.ExternalNavigator] pattern.
 */
internal interface AndroidAppHost {
  fun finishApp()

  fun applyEdgeToEdgeStyle(systemBarStyle: SystemBarStyle)

  fun shouldShowPermissionRationale(permission: String): Boolean

  fun tryShowAppStoreReviewDialog()
}
