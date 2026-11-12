package com.hedvig.android.feature.profile.aboutapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import hedvig.resources.Res
import hedvig.resources.app_info_submit_bug_prefilled_letter_body
import hedvig.resources.app_info_submit_bug_prefilled_letter_subject
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.net.URLEncoder
import org.jetbrains.compose.resources.stringResource

@Composable
internal actual fun rememberOpenBugReportEmail(
  memberId: String?,
  deviceId: String?,
  appVersionName: String,
): () -> Unit {
  val letterSubject = stringResource(Res.string.app_info_submit_bug_prefilled_letter_subject)
  val letterBody = stringResource(
    Res.string.app_info_submit_bug_prefilled_letter_body,
    memberId.toString(),
    deviceId.toString(),
    appVersionName,
    "${System.getProperty("os.name")} ${System.getProperty("os.version")}",
  )
  return remember(letterSubject, letterBody) {
    { openMailClient("android@hedvig.com", letterSubject, letterBody) }
  }
}

private fun openMailClient(email: String, letterSubject: String, letterBody: String) {
  val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
  if (desktop?.isSupported(Desktop.Action.MAIL) != true) {
    logcat(LogPriority.WARN) { "Cannot open an email client on this computer" }
    return
  }
  val uri = URI("mailto:$email?subject=${letterSubject.mailtoEncoded()}&body=${letterBody.mailtoEncoded()}")
  try {
    desktop.mail(uri)
  } catch (e: IOException) {
    logcat(LogPriority.WARN, e) { "No email client could be launched on this computer" }
  } catch (e: SecurityException) {
    logcat(LogPriority.WARN, e) { "Not allowed to launch the email client on this computer" }
  } catch (e: UnsupportedOperationException) {
    logcat(LogPriority.WARN, e) { "Cannot open an email client on this computer" }
  }
}

/** Percent-encodes for a mailto query, where a space must be %20 rather than the form encoding's +. */
private fun String.mailtoEncoded(): String = URLEncoder.encode(this, Charsets.UTF_8).replace("+", "%20")
