package com.hedvig.android.feature.connect.payment.adyen

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.composewebview.AccompanistWebViewClient
import com.hedvig.android.composewebview.LoadingState
import com.hedvig.android.composewebview.WebView
import com.hedvig.android.composewebview.rememberSaveableWebViewState
import com.hedvig.android.composewebview.rememberWebViewNavigator
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.component.success.HedvigSuccessSection
import com.hedvig.android.core.ui.appbar.m3.TopAppBarWithBack
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import hedvig.resources.R

@Composable
internal fun AdyenDestination(viewModel: AdyenViewModel, navigateUp: () -> Unit, finishAdyenFlow: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  AdyenScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    connectingCardSucceeded = { viewModel.emit(AdyenEvent.ConnectingCardSucceeded) },
    connectingCardFailed = { viewModel.emit(AdyenEvent.ConnectingCardFailed) },
    retryConnectingCard = { viewModel.emit(AdyenEvent.RetryLoadingPaymentLink) },
    finishAdyenFlow = finishAdyenFlow,
  )
}

@Composable
private fun AdyenScreen(
  uiState: AdyenUiState,
  navigateUp: () -> Unit,
  connectingCardSucceeded: () -> Unit,
  connectingCardFailed: () -> Unit,
  retryConnectingCard: () -> Unit,
  finishAdyenFlow: () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.background,
    modifier = Modifier.fillMaxSize(),
  ) {
    when (uiState) {
      AdyenUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgress()
      }
      is AdyenUiState.Browsing -> {
        AdyenBrowser(
          uiState,
          navigateUp,
          connectingCardSucceeded,
          connectingCardFailed,
        )
      }
      AdyenUiState.FailedToConnectCard -> {
        HedvigErrorSection(
          retry = retryConnectingCard,
          title = stringResource(R.string.something_went_wrong),
          subTitle = stringResource(R.string.pay_in_error_body),
        )
      }
      AdyenUiState.FailedToGetPaymentLink -> {
        HedvigErrorSection(
          retry = retryConnectingCard,
          title = stringResource(R.string.something_went_wrong),
          subTitle = null,
        )
      }
      AdyenUiState.SucceededInConnectingCard -> {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          HedvigSuccessSection(
            title = stringResource(R.string.pay_in_confirmation_headline),
            subTitle = null,
            withDefaultVerticalSpacing = false,
          )
          Spacer(Modifier.height(24.dp))
          HedvigContainedSmallButton(
            text = stringResource(R.string.general_done_button),
            onClick = finishAdyenFlow,
          )
        }
      }
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun AdyenBrowser(
  uiState: AdyenUiState.Browsing,
  navigateUp: () -> Unit,
  connectingCardSucceeded: () -> Unit,
  connectingCardFailed: () -> Unit,
) {
  val webViewState = rememberSaveableWebViewState()
  val webViewNavigator = rememberWebViewNavigator()
  val webViewClient = remember {
    object : AccompanistWebViewClient() {
      override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        logcat { "Adyen Webview loading url:$url" }
        if (url?.contains("success") == true) {
          logcat { "Adyen Webview overrides success url:$url" }
          view.stopLoading()
          webViewNavigator.stopLoading()
          connectingCardSucceeded()
          return
        }
        if (url?.contains("fail") == true) {
          logcat { "Adyen Webview overrides fail url:$url" }
          view.stopLoading()
          webViewNavigator.stopLoading()
          connectingCardFailed()
          return
        }
      }

      override fun onPageFinished(view: WebView, url: String?) {
        super.onPageFinished(view, url)
        logcat { "Adyen Webview finished loading url:$url" }
      }

      override fun onReceivedError(view: WebView, request: WebResourceRequest?, error: WebResourceError?) {
        super.onReceivedError(view, request, error)
        logcat(LogPriority.WARN) { "Webview got error:$error for request:$request" }
      }
    }
  }

  val adyenPaymentUrl = (uiState as? AdyenUiState.Browsing)?.adyenPaymentUrl
  LaunchedEffect(adyenPaymentUrl) {
    if (adyenPaymentUrl != null) {
      webViewNavigator.loadUrl(adyenPaymentUrl.url)
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
      val loadingState = webViewState.loadingState
      if (loadingState is LoadingState.Loading) {
        LinearProgressIndicator(
          progress = { loadingState.progress },
          modifier = Modifier.fillMaxWidth(),
        )
      }
      WebView(
        state = webViewState,
        navigator = webViewNavigator,
        onCreated = { webView ->
          webView.settings.javaScriptEnabled = true
        },
        client = webViewClient,
        modifier = Modifier.matchParentSize(),
      )
    }
  }
}
