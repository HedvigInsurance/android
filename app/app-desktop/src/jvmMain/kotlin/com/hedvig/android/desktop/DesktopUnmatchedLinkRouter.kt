package com.hedvig.android.desktop

import com.hedvig.android.app.navigation.BackstackController
import com.hedvig.android.core.buildconstants.Flavor
import com.hedvig.android.core.buildconstants.deepLinkHostsOf
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.compose.HedvigDeepLinkMatcher
import java.net.URI
import java.net.URISyntaxException

/**
 * Keeps links on Hedvig's deep link hosts inside the app when no deep link matcher handled them, since nothing routes
 * such a link back into the desktop app once it opens in the browser.
 *
 * - A link on a deep link host of [runningFlavor] lands on the Home tab, as an unknown path on our hosts does on
 *   Android. Logged out, nothing happens: logging in lands on Home anyway.
 * - A link on a production deep link host while another flavor runs is moved to the running flavor's host of the same
 *   kind and matched again. A match navigates like an in-app link tap (stashed until login while logged out); no match
 *   lands on the Home tab.
 * - [route] returns false for every other link, which the caller then opens outside the app.
 */
internal class DesktopUnmatchedLinkRouter(
  private val runningFlavor: Flavor,
  private val matcher: HedvigDeepLinkMatcher,
  private val backstackController: BackstackController,
) {
  private val runningHosts = deepLinkHostsOf(runningFlavor).map(::DeepLinkHost)
  private val productionHosts = deepLinkHostsOf(Flavor.Production).map(::DeepLinkHost)

  fun route(uri: String): Boolean {
    val parsed = try {
      URI(uri)
    } catch (e: URISyntaxException) {
      return false
    }
    if (!parsed.scheme.equals("https", ignoreCase = true)) return false
    if (runningHosts.any { it.matches(parsed) }) {
      logcat { "No destination matches the Hedvig link $uri, landing on Home" }
      landOnHome()
      return true
    }
    if (runningFlavor == Flavor.Production) return false
    val productionIndex = productionHosts.indexOfFirst { it.matches(parsed) }
    if (productionIndex == -1) return false
    val rewritten = runningHosts[productionIndex].rewrite(parsed, from = productionHosts[productionIndex])
    val destination = matcher.match(rewritten)
    if (destination != null) {
      logcat { "Opening the production link $uri as $rewritten" }
      backstackController.navigateToInAppLink(destination)
    } else {
      logcat { "No destination matches the production link $uri as $rewritten, landing on Home" }
      landOnHome()
    }
    return true
  }

  private fun landOnHome() {
    if (backstackController.isLoggedIn) {
      backstackController.selectTopLevel(TopLevelTab.Home)
    }
  }
}

/** One entry of [deepLinkHostsOf]: a host, and the path prefix links on it start with, which may be empty. */
private class DeepLinkHost(entry: String) {
  private val host: String
  private val pathPrefix: String

  init {
    val entryUri = URI("https://$entry")
    host = entryUri.host
    pathPrefix = entryUri.rawPath.orEmpty()
  }

  fun matches(uri: URI): Boolean {
    if (!host.equals(uri.host, ignoreCase = true)) return false
    val path = uri.rawPath.orEmpty()
    return pathPrefix.isEmpty() || path == pathPrefix || path.startsWith("$pathPrefix/")
  }

  /** [uri], which [from] matches, moved onto this host with its path below the prefix, query and fragment kept. */
  fun rewrite(uri: URI, from: DeepLinkHost): String = buildString {
    append("https://")
    append(host)
    append(pathPrefix)
    append(uri.rawPath.orEmpty().removePrefix(from.pathPrefix))
    uri.rawQuery?.let { append('?').append(it) }
    uri.rawFragment?.let { append('#').append(it) }
  }
}
