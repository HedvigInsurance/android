package com.hedvig.android.feature.connect.payment.trustly.ui

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.hedvig.android.composewebview.LoadingState
import com.hedvig.android.composewebview.WebView
import com.hedvig.android.composewebview.rememberSaveableWebViewState
import com.hedvig.android.composewebview.rememberWebViewNavigator
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.feature.connect.payment.trustly.sdk.TrustlyWebChromeClient
import com.hedvig.android.feature.connect.payment.trustly.sdk.TrustlyWebView
import com.hedvig.android.feature.connect.payment.trustly.sdk.TrustlyWebViewClient
import com.hedvig.android.logger.logcat

@Composable
internal actual fun TrustlyCheckoutWebView(
  url: String,
  connectingCardSucceeded: () -> Unit,
  connectingCardFailed: () -> Unit,
  connectingCardCancelled: () -> Unit,
  modifier: Modifier,
) {
  val webViewState = rememberSaveableWebViewState()
  val webViewNavigator = rememberWebViewNavigator()

  LaunchedEffect(url) {
    webViewNavigator.loadUrl(url)
  }

  Box(modifier) {
    WebView(
      factory = { context ->
        TrustlyWebView(
          activity = context as Activity,
          successHandler = {
            logcat { "Trustly Webview: successHandler" }
            connectingCardSucceeded()
          },
          errorHandler = {
            logcat { "Trustly Webview: errorHandler" }
            connectingCardFailed()
          },
          abortHandler = {
            logcat { "Trustly Webview: abortHandler" }
            connectingCardCancelled()
          },
        )
      },
      state = webViewState,
      navigator = webViewNavigator,
      client = remember { TrustlyWebViewClient() },
      chromeClient = remember { TrustlyWebChromeClient() },
      modifier = Modifier.matchParentSize(),
    )
    val loadingState = webViewState.loadingState
    if (loadingState is LoadingState.Loading) {
      val brushColor = HedvigTheme.colorScheme.fillPrimary
      Canvas(
        Modifier
          .fillMaxWidth()
          .height(4.dp),
      ) {
        drawRect(brushColor, size = Size(size.width * loadingState.progress, size.height))
      }
    }
  }
}
