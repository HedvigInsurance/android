package com.hedvig.android.feature.connect.payment.trustly.sdk

import android.graphics.Bitmap
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.hedvig.android.composewebview.AccompanistWebViewClient
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

class TrustlyWebViewClient : AccompanistWebViewClient() {
  override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
    logcat { "Trustly Webview loading url:$url" }
  }

  override fun onPageFinished(view: WebView, url: String?) {
    super.onPageFinished(view, url)
    logcat { "Trustly Webview finished loading url:$url" }
  }

  override fun onReceivedError(view: WebView, request: WebResourceRequest?, error: WebResourceError?) {
    super.onReceivedError(view, request, error)
    logcat(LogPriority.WARN) {
      buildString {
        append("Webview got error:(")
        append("(${error?.errorCode}: ${error?.description})")
        append(" for request:")
        append("(${request?.url} | ${request?.method}")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          append(" | ")
          append(request?.isRedirect)
        }
        append(")")
      }
    }
  }
}
