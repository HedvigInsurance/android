package com.hedvig.android.feature.connect.payment.trustly.ui

import android.app.Activity
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.composewebview.LoadingState
import com.hedvig.android.composewebview.WebView
import com.hedvig.android.composewebview.rememberSaveableWebViewState
import com.hedvig.android.composewebview.rememberWebViewNavigator
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.Button
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.SUCCESS
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
import com.hedvig.android.feature.connect.payment.trustly.TrustlyEvent
import com.hedvig.android.feature.connect.payment.trustly.TrustlyUiState
import com.hedvig.android.feature.connect.payment.trustly.TrustlyViewModel
import com.hedvig.android.feature.connect.payment.trustly.data.PreviewTrustlyCallback
import com.hedvig.android.feature.connect.payment.trustly.sdk.TrustlyWebChromeClient
import com.hedvig.android.feature.connect.payment.trustly.sdk.TrustlyWebView
import com.hedvig.android.feature.connect.payment.trustly.sdk.TrustlyWebViewClient
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.common.Destination
import hedvig.resources.Res
import hedvig.resources.general_done_button
import hedvig.resources.pay_in_confirmation_direct_debit_headline
import hedvig.resources.pay_in_error_body
import hedvig.resources.pay_in_explainer_direct_debit_headline
import hedvig.resources.something_went_wrong
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource

@Serializable
data object TrustlyDestination : Destination

@Composable
internal fun TrustlyDestination(viewModel: TrustlyViewModel, navigateUp: () -> Unit, finishTrustlyFlow: () -> Unit) {
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
          title = stringResource(Res.string.something_went_wrong),
          subTitle = stringResource(Res.string.pay_in_error_body),
        )
      }

      TrustlyUiState.FailedToStartSession -> {
        HedvigErrorSection(
          onButtonClick = retryConnectingCard,
          title = stringResource(Res.string.something_went_wrong),
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
            text = stringResource(Res.string.pay_in_confirmation_direct_debit_headline),
            description = null,
            buttonStyle = Button(stringResource(Res.string.general_done_button), finishTrustlyFlow),
          )
        }
      }
    }
  }
}

@Composable
private fun TrustlyBrowser(
  uiState: TrustlyUiState.Browsing,
  navigateUp: () -> Unit,
  connectingCardSucceeded: () -> Unit,
  connectingCardFailed: () -> Unit,
) {
  val webViewState = rememberSaveableWebViewState()
  val webViewNavigator = rememberWebViewNavigator()

  LaunchedEffect(uiState.url) {
    webViewNavigator.loadUrl(uiState.url)
  }

  Column {
    TopAppBarWithBack(
      title = stringResource(Res.string.pay_in_explainer_direct_debit_headline),
      onClick = navigateUp,
    )
    Box(
      Modifier
        .weight(1f)
        .fillMaxWidth()
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)),
    ) {
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
              connectingCardFailed()
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
}

@HedvigPreview
@Composable
private fun TrustlyPreview(
  @PreviewParameter(TrustlyUiStateProvider::class) uiState: TrustlyUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TrustlyScreen(
        uiState,
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

private class TrustlyUiStateProvider :
  CollectionPreviewParameterProvider<TrustlyUiState>(
    listOf(
      TrustlyUiState.Browsing("", PreviewTrustlyCallback("", "")),
      TrustlyUiState.Loading,
      TrustlyUiState.FailedToConnectCard,
      TrustlyUiState.FailedToStartSession,
      TrustlyUiState.SucceededInConnectingCard,
    ),
  )
