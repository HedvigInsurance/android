package com.hedvig.android.desktop

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.UriHandler
import com.hedvig.android.app.AppHost
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import java.net.URI

/**
 * The [AppHost] of the desktop window. Links open in the system browser; there is no permission rationale, store
 * review prompt or system bar on desktop.
 */
internal class DesktopAppHost(
  private val desktopIntegration: DesktopIntegration = AwtDesktopIntegration,
  private val showNotice: (String) -> Unit = ::showDesktopNotice,
) : AppHost {
  override val uriHandler: UriHandler = object : UriHandler {
    override fun openUri(uri: String) {
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
