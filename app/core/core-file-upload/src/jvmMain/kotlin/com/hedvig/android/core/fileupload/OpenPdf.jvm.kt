package com.hedvig.android.core.fileupload

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import java.awt.Desktop
import java.awt.EventQueue
import java.awt.GraphicsEnvironment
import java.awt.KeyboardFocusManager
import java.io.File
import java.io.IOException
import javax.swing.JOptionPane

@Composable
actual fun rememberOpenPdf(applicationId: String): (DownloadedFile) -> Unit {
  return remember {
    { downloadedFile -> openPdf(File(downloadedFile.path)) }
  }
}

/**
 * Opens [file] in the system's PDF viewer. Every way that can fail goes to [reportFailure] instead of throwing.
 */
internal fun openPdf(
  file: File,
  isDesktopSupported: () -> Boolean = Desktop::isDesktopSupported,
  isOpenSupported: () -> Boolean = { Desktop.getDesktop().isSupported(Desktop.Action.OPEN) },
  open: (File) -> Unit = { Desktop.getDesktop().open(it) },
  reportFailure: (file: File, cause: Throwable?) -> Unit = ::reportOpenPdfFailure,
) {
  if (!file.isFile) {
    reportFailure(file, null)
    return
  }
  try {
    if (!isDesktopSupported() || !isOpenSupported()) {
      reportFailure(file, null)
      return
    }
    open(file)
  } catch (e: IOException) {
    reportFailure(file, e)
  } catch (e: UnsupportedOperationException) {
    reportFailure(file, e)
  } catch (e: SecurityException) {
    reportFailure(file, e)
  } catch (e: IllegalArgumentException) {
    reportFailure(file, e)
  }
}

/**
 * Logs the failure and, when there is a screen, tells the member where the PDF is so they can open it themselves.
 */
internal fun reportOpenPdfFailure(
  file: File,
  cause: Throwable?,
  isHeadless: () -> Boolean = GraphicsEnvironment::isHeadless,
  showDialog: (message: String) -> Unit = ::showWarningDialog,
) {
  logcat(LogPriority.WARN, cause) { "Could not open the downloaded PDF in a viewer" }
  if (isHeadless()) return
  showDialog("Could not open the PDF on this computer. It is saved at:\n${file.absolutePath}")
}

private fun showWarningDialog(message: String) {
  val show = {
    JOptionPane.showMessageDialog(
      KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow,
      message,
      "Hedvig",
      JOptionPane.WARNING_MESSAGE,
    )
  }
  if (EventQueue.isDispatchThread()) show() else EventQueue.invokeLater(show)
}
