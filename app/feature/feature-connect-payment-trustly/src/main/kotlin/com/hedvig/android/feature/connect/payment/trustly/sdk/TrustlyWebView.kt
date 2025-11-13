package com.hedvig.android.feature.connect.payment.trustly.sdk

import android.app.Activity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.webkit.WebView
import android.webkit.WebViewClient
import com.hedvig.android.feature.connect.payment.trustly.webview.TrustlyWebChromeClient
import com.hedvig.android.logger.logcat


class TrustlyWebView(
  activity: Activity,
  url: String,
  val successHandler: TrustlyCheckoutSuccessHandler?,
  val errorHandler: TrustlyCheckoutErrorHandler?,
  val abortHandler: TrustlyCheckoutAbortHandler?,
) : WebView(activity) {
  init {
    tryOpeningUrlInWebView(activity, url)
  }

  private fun tryOpeningUrlInWebView(activity: Activity, url: String) {
    try {
      configWebSettings()
      webViewClient = WebViewClient()
      webChromeClient = TrustlyWebChromeClient()
      addJavascriptInterface(TrustlyJavascriptInterface(activity, this), TrustlyJavascriptInterface.NAME)
      layoutParams = LayoutParams(LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
      loadUrl(url)
    } catch (e: Exception) {
      if (e is WebSettingsException) {
        logcat { "TrustlyWebView: configWebView: Could not config WebSettings | ${e.message}" }
      } else {
        logcat { "TrustlyWebView: configWebView: Unknown Problem happened | ${e.message}" }
      }
    }
  }

  @Throws(WebSettingsException::class)
  private fun configWebSettings() {
    try {
      val webSettings = getSettings()
      webSettings.javaScriptEnabled = true
      webSettings.domStorageEnabled = true
      webSettings.javaScriptCanOpenWindowsAutomatically = true
      webSettings.setSupportMultipleWindows(true)
    } catch (e: Exception) {
      throw WebSettingsException(e.message)
    }
  }
}
