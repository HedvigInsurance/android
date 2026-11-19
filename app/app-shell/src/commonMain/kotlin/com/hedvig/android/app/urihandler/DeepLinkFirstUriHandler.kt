package com.hedvig.android.app.urihandler

import androidx.compose.ui.platform.UriHandler
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.logger.logcat
import com.hedvig.android.logger.memberData
import com.hedvig.android.logger.urlForLog
import com.hedvig.android.logger.withoutUrlQueries
import com.hedvig.android.navigation.compose.HedvigDeepLinkMatcher

/**
 * Handles *in-app* link taps (the Compose [UriHandler] behind `LocalUriHandler`). Tries to resolve a
 * URI to an in-app [com.hedvig.android.navigation.common.HedvigNavKey] via [matcher] and navigate to
 * it via [backstackController], appending onto the live stack. Falls back to the [delegate] (e.g. the
 * system browser) when the URI is not one of our deep links — correct here because the member tapped a
 * link inside the app. External / notification deep links are handled by [ExternalDeepLinkHandler],
 * which lands alone and never falls back to the browser.
 */
internal class DeepLinkFirstUriHandler(
  private val matcher: HedvigDeepLinkMatcher,
  private val backstackController: BackstackController,
  private val delegate: UriHandler,
) : UriHandler {
  override fun openUri(uri: String) {
    val destination = matcher.match(uri)
    if (destination != null) {
      logcat {
        val loggedDestination = memberData(destination, destination.toString().withoutUrlQueries())
        "DeepLinkFirstUriHandler navigating to $loggedDestination for uri:${urlForLog(uri)}"
      }
      backstackController.navigateToInAppLink(destination)
    } else {
      logcat { "DeepLinkFirstUriHandler falling back to default UriHandler for uri:${urlForLog(uri)}" }
      delegate.openUri(uri)
    }
  }
}
