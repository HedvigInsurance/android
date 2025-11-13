package com.hedvig.android.feature.connect.payment.trustly.sdk

import android.annotation.SuppressLint
import android.app.Activity
import android.webkit.WebView
import com.hedvig.android.logger.logcat

@SuppressLint("ViewConstructor")
class TrustlyWebView(
  activity: Activity,
  val successHandler: TrustlyCheckoutSuccessHandler?,
  val errorHandler: TrustlyCheckoutErrorHandler?,
  val abortHandler: TrustlyCheckoutAbortHandler?,
) : WebView(activity) {
  init {
    setupWebView(activity)
  }

  private fun setupWebView(activity: Activity) {
    try {
      configWebSettings()
      addJavascriptInterface(TrustlyJavascriptInterface(activity, this), TrustlyJavascriptInterface.NAME)
    } catch (e: Exception) {
      if (e is WebSettingsException) {
        logcat { "TrustlyWebView: configWebView: Could not config WebSettings | ${e.message}" }
      } else {
        logcat { "TrustlyWebView: configWebView: Unknown Problem happened | ${e.message}" }
      }
    }
  }

  @SuppressLint("SetJavaScriptEnabled")
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
