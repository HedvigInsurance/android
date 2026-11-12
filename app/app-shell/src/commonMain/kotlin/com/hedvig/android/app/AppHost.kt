package com.hedvig.android.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.UriHandler

/**
 * The platform capabilities the app shell needs from the host that renders it, handed to the shell as a
 * single dependency. Mirrors the [com.hedvig.android.navigation.activity.ExternalNavigator] pattern.
 */
interface AppHost {
  /** Opens a link that is not one of the app's own deep links. */
  val uriHandler: UriHandler

  fun shouldShowPermissionRationale(permission: String): Boolean

  fun tryShowAppStoreReviewDialog()

  /** Styles the system bars to match [darkTheme]. */
  @Composable
  fun SystemBarsEffect(darkTheme: Boolean)
}
