package com.hedvig.android.app.urihandler

import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.feature.home.home.navigation.HomeKey
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.compose.HedvigDeepLinkMatcher
import java.net.URI
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

/**
 * Handles deep links that enter the app from *outside*: external https App Links and notification
 * taps, both forwarded as raw URI strings from `MainActivity` via the deep-link channel.
 *
 * Three deliberate differences from the in-app [DeepLinkFirstUriHandler]:
 *  1. **Never falls back to the browser.** The host is one of our autoVerify'd App Link domains, so
 *     opening it externally would bounce straight back into `MainActivity` as a fresh `ACTION_VIEW` —
 *     an infinite self-relaunch. "Open it in the browser" is only ever correct for an in-app tap on a
 *     link we don't handle.
 *  2. **Unknown path on one of *our* hosts lands on Home.** A URI we can't match to a specific screen
 *     but that targets one of [deepLinkHosts] (e.g. `.../home`, or any future path the backend sends
 *     before the app learns it) lands the member on Home rather than being silently dropped. Only a
 *     URI on a foreign host is ignored. This is the real "unknown deep link -> Home" behaviour the
 *     matcher itself can't express (Nav3 patterns are exact, with no catch-all).
 *  3. **Gated on [readySignal].** We wait for the [com.hedvig.android.app.navigation.SessionReconciler]
 *     to resolve the start scene before routing, so the link reads a settled auth state and lands
 *     deterministically — rather than racing the splash-gated reconciliation.
 *
 * A matched key (or the Home fallback) lands *alone* (see [BackstackController.navigateToExternalDeepLink]);
 * the ancestry is rebuilt on demand by Up.
 */
internal class ExternalDeepLinkHandler(
  private val matcher: HedvigDeepLinkMatcher,
  private val backstackController: BackstackController,
  private val readySignal: StateFlow<Boolean>,
  private val deepLinkHosts: List<String>,
) {
  suspend fun handle(uri: String) {
    readySignal.first { it }
    val destination = matcher.match(uri)
    if (destination != null) {
      logcat { "ExternalDeepLinkHandler landing external deep link $destination for uri:$uri" }
      backstackController.navigateToExternalDeepLink(destination)
      return
    }
    if (targetsOwnDeepLink(uri)) {
      logcat(LogPriority.WARN) { "ExternalDeepLinkHandler no specific match for own-host uri:$uri — falling back to Home" }
      backstackController.navigateToExternalDeepLink(HomeKey)
      return
    }
    logcat(LogPriority.ERROR) { "ExternalDeepLinkHandler ignoring unmatched foreign-host external deep link uri:$uri" }
  }

  /**
   * True when [uri] targets one of our deep-link hosts. [deepLinkHosts] entries are bare `host`
   * (e.g. `link.hedvig.com`) or `host/pathPrefix` (e.g. `www.hedvig.com/deeplink`), matching the
   * manifest's autoVerify filters — so we parse each entry as a URI too and require the host to match
   * *and* the path to start with the entry's path prefix. An empty entry path matches any path.
   */
  private fun targetsOwnDeepLink(uri: String): Boolean {
    val parsed = runCatching { URI(uri) }.getOrNull() ?: return false
    val host = parsed.host ?: return false
    val path = parsed.path ?: ""
    return deepLinkHosts.any { entry ->
      val entryUri = runCatching { URI("https://$entry") }.getOrNull() ?: return@any false
      val entryHost = entryUri.host ?: return@any false
      host.equals(entryHost, ignoreCase = true) && path.startsWith(entryUri.path ?: "")
    }
  }
}
