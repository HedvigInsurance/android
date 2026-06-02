package com.hedvig.android.app.urihandler

import androidx.compose.ui.platform.UriHandler
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.compose.HedvigDeepLinkMatcher
import com.hedvig.android.navigation.compose.Navigator

/**
 * Tries to resolve a URI to an in-app [com.hedvig.android.navigation.common.HedvigNavKey] via [matcher] and navigate to
 * it with [navigator]. Falls back to the [delegate] (e.g. the system browser) when the URI is not one of our deep
 * links.
 */
internal class DeepLinkFirstUriHandler(
  private val matcher: HedvigDeepLinkMatcher,
  private val navigator: Navigator,
  private val delegate: UriHandler,
) : UriHandler {
  override fun openUri(uri: String) {
    val destination = matcher.match(uri)
    if (destination != null) {
      logcat { "DeepLinkFirstUriHandler navigating to $destination for uri:$uri" }
      navigator.navigate(destination)
    } else {
      logcat { "DeepLinkFirstUriHandler falling back to default UriHandler for uri:$uri" }
      delegate.openUri(uri)
    }
  }
}
