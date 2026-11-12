package com.hedvig.android.feature.profile.aboutapp

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import hedvig.resources.Res
import hedvig.resources.app_info_submit_bug_prefilled_letter_body
import hedvig.resources.app_info_submit_bug_prefilled_letter_subject
import org.jetbrains.compose.resources.stringResource

@Composable
internal actual fun rememberOpenBugReportEmail(
  memberId: String?,
  deviceId: String?,
  appVersionName: String,
): () -> Unit {
  val localContext = LocalContext.current
  val letterSubject = stringResource(Res.string.app_info_submit_bug_prefilled_letter_subject)
  val letterBody = String.format(
    stringResource(Res.string.app_info_submit_bug_prefilled_letter_body),
    memberId,
    deviceId,
    appVersionName,
    "Android ${Build.VERSION.SDK_INT}",
  )
  return remember(localContext, letterSubject, letterBody) {
    {
      openEmailClientWithPrefilledData(
        localContext,
        "android@hedvig.com",
        letterSubject,
        letterBody,
      )
    }
  }
}

private fun openEmailClientWithPrefilledData(
  context: Context,
  email: String,
  letterSubject: String,
  letterBody: String,
) {
  val sendLetterIntent: Intent = Intent().apply {
    action = Intent.ACTION_SENDTO
    data = "mailto:".toUri()
    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
    putExtra(Intent.EXTRA_SUBJECT, letterSubject)
    putExtra(Intent.EXTRA_TEXT, letterBody)
  }
  context.startActivity(
    Intent.createChooser(
      sendLetterIntent,
      letterSubject,
    ),
  )
}
