package com.hedvig.android.feature.connect.payment.trustly.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.hedvig.android.composewebview.AccompanistWebViewClient
import com.hedvig.android.composewebview.LoadingState
import com.hedvig.android.composewebview.WebView
import com.hedvig.android.composewebview.rememberSaveableWebViewState
import com.hedvig.android.composewebview.rememberWebViewNavigator
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.Button
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.SUCCESS
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
import com.hedvig.android.feature.connect.payment.trustly.TrustlyEvent
import com.hedvig.android.feature.connect.payment.trustly.TrustlyUiState
import com.hedvig.android.feature.connect.payment.trustly.TrustlyViewModel
import com.hedvig.android.feature.connect.payment.trustly.webview.TrustlyJavascriptInterface
import com.hedvig.android.feature.connect.payment.trustly.webview.TrustlyWebChromeClient
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.market.Market
import com.hedvig.android.navigation.compose.Destination
import hedvig.resources.R
import kotlinx.serialization.Serializable

@Serializable
data object TrustlyDestination : Destination

@Composable
internal fun TrustlyDestination(
  viewModel: TrustlyViewModel,
  market: Market,
  navigateUp: () -> Unit,
  finishTrustlyFlow: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
) {
  if (market != Market.SE) {
    NonSwedishScreen(navigateUp, onNavigateToNewConversation)
  } else {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TrustlyScreen(
      uiState = uiState,
      navigateUp = navigateUp,
      connectingCardSucceeded = { viewModel.emit(TrustlyEvent.ConnectingCardSucceeded) },
      connectingCardFailed = { viewModel.emit(TrustlyEvent.ConnectingCardFailed) },
      retryConnectingCard = { viewModel.emit(TrustlyEvent.RetryConnectingCard) },
      finishTrustlyFlow = finishTrustlyFlow,
    )
  }
}

@Composable
private fun TrustlyScreen(
  uiState: TrustlyUiState,
  navigateUp: () -> Unit,
  connectingCardSucceeded: () -> Unit,
  connectingCardFailed: () -> Unit,
  retryConnectingCard: () -> Unit,
  finishTrustlyFlow: () -> Unit,
) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.fillMaxSize(),
  ) {
    when (uiState) {
      TrustlyUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgress()
      }

      is TrustlyUiState.Browsing -> {
        TrustlyBrowser(
          uiState,
          navigateUp,
          connectingCardSucceeded,
          connectingCardFailed,
        )
      }

      TrustlyUiState.FailedToConnectCard -> {
        HedvigErrorSection(
          onButtonClick = retryConnectingCard,
          title = stringResource(R.string.something_went_wrong),
          subTitle = stringResource(R.string.pay_in_error_body),
        )
      }

      TrustlyUiState.FailedToStartSession -> {
        HedvigErrorSection(
          onButtonClick = retryConnectingCard,
          title = stringResource(R.string.something_went_wrong),
          subTitle = null,
        )
      }

      TrustlyUiState.SucceededInConnectingCard -> {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          EmptyState(
            iconStyle = SUCCESS,
            text = stringResource(R.string.pay_in_confirmation_direct_debit_headline),
            description = null,
            buttonStyle = Button(stringResource(R.string.general_done_button), finishTrustlyFlow),
          )
        }
      }
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun TrustlyBrowser(
  uiState: TrustlyUiState.Browsing,
  navigateUp: () -> Unit,
  connectingCardSucceeded: () -> Unit,
  connectingCardFailed: () -> Unit,
) {
  val context = LocalContext.current
  val webViewState = rememberSaveableWebViewState()
  val webViewNavigator = rememberWebViewNavigator()

  val webViewClient = remember {
    object : AccompanistWebViewClient() {
      override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        logcat { "Trustly Webview loading url:$url" }

        if (url?.startsWith("bankid") == true) {
          logcat(LogPriority.ERROR) { "Url did in fact try to open bankid" }
          view.stopLoading()
          val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
          context.startActivity(intent)
          return
        }
        if (url == uiState.trustlyCallback.successUrl) {
          logcat { "Trustly Webview overrides success url:$url" }
          view.stopLoading()
          webViewNavigator.stopLoading()
          connectingCardSucceeded()
          return
        }
        if (url == uiState.trustlyCallback.failureUrl) {
          logcat { "Trustly Webview overrides fail url:$url" }
          view.stopLoading()
          webViewNavigator.stopLoading()
          connectingCardFailed()
          return
        }
      }

      override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        logcat { "Trustly Webview finished loading url:$url" }
      }

      override fun onReceivedError(view: WebView, request: WebResourceRequest?, error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        logcat(LogPriority.WARN) { "Webview got error:$error for request:$request" }
      }
    }
  }

  val url = (uiState as? TrustlyUiState.Browsing)?.url
  LaunchedEffect(url) {
    if (url != null) {
      webViewNavigator.loadUrl(url)
    }
  }

  Column {
    TopAppBarWithBack(
      title = stringResource(R.string.PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_TITLE),
      onClick = {
        if (webViewNavigator.canGoBack) {
          webViewNavigator.navigateBack()
        } else {
          navigateUp()
        }
      },
    )
    Box(
      Modifier
        .weight(1f)
        .fillMaxWidth()
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)),
    ) {
      WebView(
        state = webViewState,
        navigator = webViewNavigator,
        onCreated = { webView ->
          webView.settings.javaScriptEnabled = true
          webView.settings.javaScriptCanOpenWindowsAutomatically = true
          webView.settings.domStorageEnabled = true
          webView.settings.setSupportMultipleWindows(true)

          webView.webChromeClient = TrustlyWebChromeClient()
          webView.addJavascriptInterface(
            TrustlyJavascriptInterface(activity = context as Activity),
            TrustlyJavascriptInterface.NAME,
          )
        },
        client = webViewClient,
        modifier = Modifier.matchParentSize(),
      )
      val loadingState = webViewState.loadingState
      if (loadingState is LoadingState.Loading) {
        val brushColor = HedvigTheme.colorScheme.fillPrimary
        Canvas(Modifier.fillMaxWidth().height(4.dp)) {
          drawRect(brushColor, size = Size(size.width * loadingState.progress, size.height))
        }
      }
    }
  }
}

@Composable
private fun NonSwedishScreen(navigateUp: () -> Unit, onNavigateToNewConversation: () -> Unit) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column {
      TopAppBarWithBack(onClick = dropUnlessResumed { navigateUp() }, title = "")
      HedvigErrorSection(
        title = stringResource(R.string.MOVEINTENT_GENERIC_ERROR),
        subTitle = null,
        onButtonClick = dropUnlessResumed { onNavigateToNewConversation() },
        buttonText = stringResource(R.string.open_chat),
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
      )
    }
  }
}
