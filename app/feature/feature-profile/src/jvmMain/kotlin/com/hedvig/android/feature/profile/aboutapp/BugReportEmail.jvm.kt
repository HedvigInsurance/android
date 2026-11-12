package com.hedvig.android.feature.profile.aboutapp

import androidx.compose.runtime.Composable
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import hedvig.resources.Res
import hedvig.resources.app_info_submit_bug_prefilled_letter_body
import hedvig.resources.app_info_submit_bug_prefilled_letter_subject
import java.awt.Desktop
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
  return { openMailClient("android@hedvig.com", letterSubject, letterBody) }
}

private fun openMailClient(email: String, letterSubject: String, letterBody: String) {
  val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
  if (desktop?.isSupported(Desktop.Action.MAIL) == true) {
    desktop.mail(URI("mailto:$email?subject=${letterSubject.mailtoEncoded()}&body=${letterBody.mailtoEncoded()}"))
  } else {
    logcat(LogPriority.WARN) { "Cannot open an email client on this computer" }
  }
}

/** Percent-encodes for a mailto query, where a space must be %20 rather than the form encoding's +. */
private fun String.mailtoEncoded(): String = URLEncoder.encode(this, Charsets.UTF_8).replace("+", "%20")
