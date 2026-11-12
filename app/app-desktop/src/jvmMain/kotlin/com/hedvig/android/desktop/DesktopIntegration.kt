package com.hedvig.android.desktop

import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import java.awt.Desktop
import java.awt.EventQueue
import java.awt.GraphicsEnvironment
import java.awt.KeyboardFocusManager
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import javax.swing.JOptionPane

/** The operating system actions the desktop host hands work to. */
internal interface DesktopIntegration {
  fun browse(uri: URI)

  fun mail()
}

/** [DesktopIntegration] through [java.awt.Desktop]. Throws [UnsupportedOperationException] where it is unsupported. */
internal object AwtDesktopIntegration : DesktopIntegration {
  override fun browse(uri: URI) {
    desktopSupporting(Desktop.Action.BROWSE).browse(uri)
  }

  override fun mail() {
    desktopSupporting(Desktop.Action.MAIL).mail()
  }

  private fun desktopSupporting(action: Desktop.Action): Desktop {
    if (!Desktop.isDesktopSupported()) throw UnsupportedOperationException("java.awt.Desktop is not supported here")
    val desktop = Desktop.getDesktop()
    if (!desktop.isSupported(action)) throw UnsupportedOperationException("$action is not supported here")
    return desktop
  }
}

/**
 * Runs [action] and reports whether it succeeded. Every way a [Desktop] action or the [URI] it is given can fail is
 * logged at WARN instead of thrown.
 */
internal fun runDesktopAction(description: String, action: () -> Unit): Boolean {
  val failure: Exception = try {
    action()
    return true
  } catch (e: IOException) {
    e
  } catch (e: UnsupportedOperationException) {
    e
  } catch (e: SecurityException) {
    e
  } catch (e: IllegalArgumentException) {
    e
  } catch (e: URISyntaxException) {
    e
  }
  logcat(LogPriority.WARN, failure) { "Could not $description on this computer" }
  return false
}

/**
 * Tells the member about something the desktop app could not do, in a dialog over the focused window, and logs it at
 * WARN. Without a screen, only the log line is written.
 */
internal fun showDesktopNotice(message: String) {
  logcat(LogPriority.WARN) { "Desktop notice: $message" }
  if (GraphicsEnvironment.isHeadless()) return
  EventQueue.invokeLater {
    JOptionPane.showMessageDialog(
      KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow,
      message,
      "Hedvig",
      JOptionPane.INFORMATION_MESSAGE,
    )
  }
}
