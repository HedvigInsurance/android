package com.hedvig.android.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.UriHandler
import com.hedvig.android.core.tracking.ErrorSource
import com.hedvig.android.core.tracking.logError
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.CancellationException

/**
 * A copy of [AndroidUriHandler][androidx.compose.ui.platform.AndroidUriHandler] which instead of crashing fallbacks to
 * doing nothing when it can not open a URI and logs this error.
 */
internal class SafeAndroidUriHandler(private val context: Context) : UriHandler {
  override fun openUri(uri: String) {
    if (uri.isEmpty()) {
      logcat(LogPriority.ERROR) { "Tried to launch an empty URI." }
      logError("Tried to launch an empty URI.", ErrorSource.SOURCE)
      return
    }
    try {
      context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
    } catch (e: Exception) {
      if (e is CancellationException) {
        throw e
      }
      logcat(LogPriority.ERROR, e) { "Tried to launch $uri but the phone has nothing to support such an intent." }
      logError("Tried to launch $uri but the phone has nothing to support such an intent.", ErrorSource.SOURCE)
    }
  }
}
