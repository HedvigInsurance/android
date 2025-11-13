package com.hedvig.android.feature.connect.payment.trustly.sdk

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.JavascriptInterface
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat


class TrustlyJavascriptInterface(
  private val activity: Activity,
  private val webViewHandler: TrustlyWebView,
) {
  @JavascriptInterface
  fun handleTrustlyEvent(typeLabel: String?, url: String?, packageName: String?) {
    val eventType = TrustlyEventType.valueForEventTypeLabel(typeLabel)
    when (eventType) {
      TrustlyEventType.SUCCESS -> webViewHandler.successHandler?.onTrustlyCheckoutSuccess()
      TrustlyEventType.ABORT -> webViewHandler.abortHandler?.onTrustlyCheckoutAbort()
      TrustlyEventType.ERROR -> webViewHandler.errorHandler?.onTrustlyCheckoutError()
      TrustlyEventType.REDIRECT -> handleRedirect(url)
      else -> {
        val errorMessage = "Unsupported event type: $typeLabel"
        logcat(LogPriority.ERROR) { errorMessage }
        throw UnsupportedOperationException(errorMessage)
      }
    }
  }

  private fun handleRedirect(urlString: String?) {
    try {
      val intent = Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(urlString)
      }
      activity.startActivityForResult(intent, 0)
    } catch (e: Error) {
      logcat { "TrustlyAndroidSDK: handleRedirect: Could not redirect to URL $urlString | ${e.message}" }
    }
  }

  companion object {
    const val NAME: String = "TrustlyAndroid"
  }
}
