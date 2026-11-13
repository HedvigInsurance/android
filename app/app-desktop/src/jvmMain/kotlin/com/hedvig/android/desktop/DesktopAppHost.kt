package com.hedvig.android.desktop

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.UriHandler
import com.hedvig.android.app.AppHost
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import java.net.URI

/**
 * The [AppHost] of the desktop window. A link no deep link matcher handled is first offered to [routeUnmatchedLink],
 * which keeps it in the app and returns true when it is on a Hedvig deep link host; any other link opens in the system
 * browser. There is no permission rationale, store review prompt or system bar on desktop.
 */
internal class DesktopAppHost(
  private val desktopIntegration: DesktopIntegration = AwtDesktopIntegration,
  private val showNotice: (String) -> Unit = ::showDesktopNotice,
  private val routeUnmatchedLink: (String) -> Boolean = { false },
) : AppHost {
  override val uriHandler: UriHandler = object : UriHandler {
    override fun openUri(uri: String) {
      if (routeUnmatchedLink(uri)) return
      val opened = runDesktopAction("open $uri in the browser") { desktopIntegration.browse(URI(uri)) }
      if (!opened) {
        showNotice("Could not open this link on this computer:\n$uri")
      }
    }
  }

  override fun shouldShowPermissionRationale(permission: String): Boolean = false

  override fun tryShowAppStoreReviewDialog() {
    logcat(LogPriority.INFO) { "The app store review prompt is not available on desktop" }
  }

  @Composable
  override fun SystemBarsEffect(darkTheme: Boolean) {
  }
}
