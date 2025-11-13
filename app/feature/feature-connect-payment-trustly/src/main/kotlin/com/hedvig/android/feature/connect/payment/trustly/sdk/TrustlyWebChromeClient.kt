package com.hedvig.android.feature.connect.payment.trustly.sdk

import android.os.Message
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebView.WebViewTransport
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import com.hedvig.android.composewebview.AccompanistWebChromeClient
import java.lang.ref.WeakReference


class TrustlyWebChromeClient : AccompanistWebChromeClient() {
  override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
    val context = view?.context ?: return false
    val tabView = WeakReference(WebView(context))
    val webview = tabView.get() ?: return false
    webview.webViewClient = Client()
    val transport = resultMsg!!.obj as WebViewTransport
    transport.webView = webview
    resultMsg.sendToTarget()
    return true
  }

  private class Client : WebViewClient() {
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
      val customTab = CustomTabsIntent.Builder().build()
      customTab.launchUrl(view.context, request.url)
      return true
    }
  }
}
