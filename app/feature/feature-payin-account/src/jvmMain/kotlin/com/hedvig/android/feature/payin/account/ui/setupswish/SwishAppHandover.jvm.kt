package com.hedvig.android.feature.payin.account.ui.setupswish

import androidx.compose.runtime.Composable
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import java.awt.Desktop
import java.net.URI

@Composable
internal actual fun rememberSwishAppHandover(allowSandboxApp: Boolean): SwishAppHandover = DesktopSwishAppHandover

/**
 * There is no Swish app on a computer, so the approval screen shows only the QR code, which the member
 * scans with Swish on their phone.
 */
internal object DesktopSwishAppHandover : SwishAppHandover {
  override val isSwishInstalled: Boolean = false

  override fun open(approvalUrl: String) {
    val uri = runCatching { URI(approvalUrl) }.getOrNull()
    val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
    if (uri != null && uri.scheme in setOf("http", "https") && desktop?.isSupported(Desktop.Action.BROWSE) == true) {
      desktop.browse(uri)
    } else {
      logcat(LogPriority.WARN) { "Cannot open the Swish approval url with scheme ${uri?.scheme} on this computer" }
    }
  }
}
