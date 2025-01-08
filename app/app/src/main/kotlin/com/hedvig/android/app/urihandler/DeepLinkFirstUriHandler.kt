package com.hedvig.android.app.urihandler

import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

/**
 * Tries to open a deep link with the [navController] and falls back to the [delegate] if the uri is not a valid deep
 * link to our app
 */
internal class DeepLinkFirstUriHandler(
  private val navController: NavController,
  private val delegate: UriHandler,
) : UriHandler {
  override fun openUri(uri: String) {
    try {
      if (navController.graph.hasDeepLink(uri.toUri())) {
        logcat { "DeepLinkFirstUriHandler will try to navigate to uri:$uri" }
        navController.navigate(uri.toUri())
        logcat { "DeepLinkFirstUriHandler did deep link to uri:$uri" }
      } else {
        logcat { "DeepLinkFirstUriHandler falling back to default UriHandler for uri:$uri" }
        delegate.openUri(uri)
      }
    } catch (e: IllegalArgumentException) {
      logcat(LogPriority.ERROR) { "Tried to open uri: $uri but got IllegalArgumentException" }
    }
  }
}
