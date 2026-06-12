package com.hedvig.android.app.urihandler

import androidx.compose.ui.platform.UriHandler
import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.feature.home.home.StartClaimSheetSignal
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.compose.HedvigDeepLinkMatcher

/**
 * Tries to resolve a URI to an in-app [com.hedvig.android.navigation.common.HedvigNavKey] via [matcher] and navigate to
 * it via [backstackController]. Falls back to the [delegate] (e.g. the system browser) when the URI is not one of our
 * deep links.
 *
 * The submit-claim deep link resolves to the same Home key as a plain Home deep link, so in addition to navigating we
 * raise [startClaimSheetSignal] when [startClaimDeepLinkRecognizer] recognizes it, asking Home to auto-open the start
 * claim sheet.
 */
internal class DeepLinkFirstUriHandler(
  private val matcher: HedvigDeepLinkMatcher,
  private val backstackController: BackstackController,
  private val delegate: UriHandler,
  private val startClaimDeepLinkRecognizer: StartClaimDeepLinkRecognizer,
  private val startClaimSheetSignal: StartClaimSheetSignal,
) : UriHandler {
  override fun openUri(uri: String) {
    val destination = matcher.match(uri)
    if (destination != null) {
      logcat { "DeepLinkFirstUriHandler navigating to $destination for uri:$uri" }
      backstackController.navigateToDeepLink(destination)
      if (startClaimDeepLinkRecognizer.isStartClaim(uri)) {
        logcat { "DeepLinkFirstUriHandler requesting start claim sheet for uri:$uri" }
        startClaimSheetSignal.request()
      }
    } else {
      logcat { "DeepLinkFirstUriHandler falling back to default UriHandler for uri:$uri" }
      delegate.openUri(uri)
    }
  }
}
