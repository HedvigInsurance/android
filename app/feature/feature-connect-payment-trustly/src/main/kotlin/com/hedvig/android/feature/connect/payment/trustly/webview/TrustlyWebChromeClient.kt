package com.hedvig.android.feature.connect.payment.trustly.webview

import android.os.Message
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import java.lang.ref.WeakReference

class TrustlyWebChromeClient : WebChromeClient() {
  override fun onCreateWindow(view: WebView, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message): Boolean {
    val tabView = WeakReference(WebView(view.context))
    tabView.get()?.let { webView ->
      webView.webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
          CustomTabsIntent.Builder().build().let { customTab ->
            customTab.launchUrl(view.context, request.url)
          }
          return true
        }
      }
      (resultMsg.obj as? WebView.WebViewTransport)?.let { transport ->
        transport.webView = webView
      }
      resultMsg.sendToTarget()
      return true
    }
    return false
  }
}
