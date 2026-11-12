package com.hedvig.android.desktop

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import java.awt.GraphicsEnvironment
import java.awt.HeadlessException
import java.awt.Toolkit

/** A large phone, so the app opens in the layout members see on their phones. */
private val preferredWindowSize = DpSize(412.dp, 915.dp)

/**
 * [preferredWindowSize], with the height lowered to [usableScreenHeight] when the screen is shorter, so the bottom of
 * the window, where the navigation bar sits, stays on screen. A null [usableScreenHeight] keeps the preferred size.
 */
internal fun initialWindowSize(usableScreenHeight: Dp?): DpSize = if (usableScreenHeight == null) {
  preferredWindowSize
} else {
  preferredWindowSize.copy(height = minOf(preferredWindowSize.height, usableScreenHeight))
}

/**
 * The height of the default screen minus its insets (menu bar, dock), in the same units as a window's size, or null
 * when there is no screen or it cannot be read.
 */
internal fun usableScreenHeight(): Dp? {
  if (GraphicsEnvironment.isHeadless()) return null
  return try {
    val configuration = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration
    val insets = Toolkit.getDefaultToolkit().getScreenInsets(configuration)
    (configuration.bounds.height - insets.top - insets.bottom).dp
  } catch (e: HeadlessException) {
    logcat(LogPriority.WARN, e) { "Could not read the screen size" }
    null
  }
}
