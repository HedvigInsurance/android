package com.hedvig.android.feature.connect.payment.trustly.ui

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.composewebview.LoadingState
import com.hedvig.android.composewebview.WebView
import com.hedvig.android.composewebview.rememberSaveableWebViewState
import com.hedvig.android.composewebview.rememberWebViewNavigator
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateButtonStyle.Button
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle.INFO
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.LoadingState as HandoverLoadingState
import com.hedvig.android.design.system.hedvig.PaymentMethodHandoverIllustration
import com.hedvig.android.design.system.hedvig.PaymentMethodMarkSize
import com.hedvig.android.design.system.hedvig.PaymentMethodTileBadge
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.Trustly
import com.hedvig.android.feature.connect.payment.trustly.TrustlyEvent
import com.hedvig.android.feature.connect.payment.trustly.TrustlyUiState
import com.hedvig.android.feature.connect.payment.trustly.sdk.TrustlyWebChromeClient
import com.hedvig.android.feature.connect.payment.trustly.sdk.TrustlyWebView
import com.hedvig.android.feature.connect.payment.trustly.sdk.TrustlyWebViewClient
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculeViewModel
import hedvig.resources.GENERAL_RETRY
import hedvig.resources.PAYMENT_CHANGE_FOOTNOTE
import hedvig.resources.PAYMENT_CHANGE_METHOD_BUTTON
import hedvig.resources.PAYMENT_TRUSTLY_FAILURE_TITLE
import hedvig.resources.PAYMENT_TRUSTLY_SUCCESS_SUBTITLE
import hedvig.resources.Res
import hedvig.resources.general_close_button
import hedvig.resources.general_continue_button
import hedvig.resources.info_card_missing_payment_body
import hedvig.resources.pay_in_confirmation_direct_debit_headline
import hedvig.resources.pay_in_error_body
import hedvig.resources.pay_in_explainer_direct_debit_headline
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TrustlyDestination(
  viewModel: MoleculeViewModel<TrustlyEvent, TrustlyUiState>,
  showSuccessScreen: Boolean,
  navigateUp: () -> Unit,
  finishTrustlyFlow: () -> Unit,
  changePaymentMethod: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val leavesWithoutConfirming = !showSuccessScreen && uiState is TrustlyUiState.SucceededInConnectingCard
  LaunchedEffect(leavesWithoutConfirming) {
    if (leavesWithoutConfirming) finishTrustlyFlow()
  }
  if (leavesWithoutConfirming) {
    // The pop is already under way, and the entry stays on screen for its exit animation, so
    // drawing the connected screen here would flash it on the way out.
    Surface(
      color = HedvigTheme.colorScheme.backgroundPrimary,
      modifier = Modifier.fillMaxSize(),
    ) {
      HedvigFullScreenCenterAlignedProgress()
    }
    return
  }
  TrustlyScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    connectingCardSucceeded = { viewModel.emit(TrustlyEvent.ConnectingCardSucceeded) },
    connectingCardFailed = { viewModel.emit(TrustlyEvent.ConnectingCardFailed) },
    connectingCardCancelled = { viewModel.emit(TrustlyEvent.ConnectingCardCancelled) },
    retryConnectingCard = { viewModel.emit(TrustlyEvent.RetryConnectingCard) },
    finishTrustlyFlow = finishTrustlyFlow,
    changePaymentMethod = changePaymentMethod,
  )
}

@Composable
private fun TrustlyScreen(
  uiState: TrustlyUiState,
  navigateUp: () -> Unit,
  connectingCardSucceeded: () -> Unit,
  connectingCardFailed: () -> Unit,
  connectingCardCancelled: () -> Unit,
  retryConnectingCard: () -> Unit,
  finishTrustlyFlow: () -> Unit,
  changePaymentMethod: () -> Unit,
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
          connectingCardCancelled,
        )
      }

      TrustlyUiState.FailedToConnectCard -> {
        TrustlyFailureScreen(
          description = stringResource(Res.string.pay_in_error_body),
          onRetry = retryConnectingCard,
          onChangePaymentMethod = changePaymentMethod,
          navigateUp = navigateUp,
        )
      }

      TrustlyUiState.CancelledConnectingCard -> {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
          Spacer(Modifier.weight(1f))
          EmptyState(
            iconStyle = INFO,
            text = stringResource(Res.string.info_card_missing_payment_body),
            description = null,
            buttonStyle = Button(stringResource(Res.string.PAYMENT_CHANGE_METHOD_BUTTON), changePaymentMethod),
          )
          Spacer(Modifier.weight(1f))
          Spacer(Modifier.height(8.dp))
          HedvigTextButton(
            text = stringResource(Res.string.general_close_button),
            onClick = finishTrustlyFlow,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
          Spacer(Modifier.height(16.dp))
        }
      }

      is TrustlyUiState.FailedToStartSession -> {
        TrustlyFailureScreen(
          description = uiState.errorMessage.message ?: stringResource(Res.string.pay_in_error_body),
          onRetry = retryConnectingCard,
          onChangePaymentMethod = changePaymentMethod,
          navigateUp = navigateUp,
        )
      }

      TrustlyUiState.SucceededInConnectingCard -> {
        TrustlyStatusScreen(
          title = stringResource(Res.string.pay_in_confirmation_direct_debit_headline),
          description = stringResource(Res.string.PAYMENT_TRUSTLY_SUCCESS_SUBTITLE),
          badge = {
            PaymentMethodTileBadge(
              icon = HedvigIcons.Checkmark,
              containerColor = HedvigTheme.colorScheme.signalGreenElement,
              contentColor = HedvigTheme.colorScheme.fillWhite,
            )
          },
          navigateUp = navigateUp,
          active = true,
        ) {
          HedvigText(
            text = stringResource(Res.string.PAYMENT_CHANGE_FOOTNOTE),
            style = HedvigTheme.typography.label,
            color = HedvigTheme.colorScheme.textSecondaryTranslucent,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
          Spacer(Modifier.height(16.dp))
          HedvigButton(
            text = stringResource(Res.string.general_continue_button),
            onClick = finishTrustlyFlow,
            enabled = true,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
        }
      }
    }
  }
}

/**
 * The handover illustration with the outcome badged onto the Hedvig symbol, matching how the Swish
 * setup reports the same two outcomes. [actions] fills the space below it.
 */
@Composable
private fun TrustlyStatusScreen(
  title: String,
  description: String?,
  active: Boolean,
  badge: @Composable () -> Unit,
  navigateUp: () -> Unit,
  actions: @Composable ColumnScope.() -> Unit,
) {
  HedvigScaffold(
    topAppBarText = null,
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    Spacer(Modifier.height(8.dp))
    FlowHeading(
      title = title,
      description = description,
      baseStyle = HedvigTheme.typography.bodySmall,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    PaymentMethodHandoverIllustration(
      modifier = Modifier.align(Alignment.CenterHorizontally),
      destinationBadge = badge,
      loadingState = if (active) HandoverLoadingState.ACTIVE else HandoverLoadingState.INACTIVE,
      mark = { Icon(HedvigIcons.Trustly, null, Modifier.size(PaymentMethodMarkSize)) },
    )
    Spacer(Modifier.weight(1f))
    actions()
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun TrustlyFailureScreen(
  description: String,
  onRetry: () -> Unit,
  onChangePaymentMethod: () -> Unit,
  navigateUp: () -> Unit,
) {
  TrustlyStatusScreen(
    title = stringResource(Res.string.PAYMENT_TRUSTLY_FAILURE_TITLE),
    description = description,
    badge = {
      PaymentMethodTileBadge(
        icon = HedvigIcons.Close,
        containerColor = HedvigTheme.colorScheme.signalAmberElement,
        contentColor = HedvigTheme.colorScheme.fillWhite,
      )
    },
    navigateUp = navigateUp,
    active = false,
  ) {
    HedvigButton(
      text = stringResource(Res.string.GENERAL_RETRY),
      onClick = onRetry,
      enabled = true,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(Res.string.PAYMENT_CHANGE_METHOD_BUTTON),
      onClick = onChangePaymentMethod,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
  }
}

@Composable
private fun TrustlyBrowser(
  uiState: TrustlyUiState.Browsing,
  navigateUp: () -> Unit,
  connectingCardSucceeded: () -> Unit,
  connectingCardFailed: () -> Unit,
  connectingCardCancelled: () -> Unit,
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
}

@HedvigPreview
@Composable
private fun TrustlyPreview(
  @PreviewParameter(TrustlyUiStateProvider::class) uiState: TrustlyUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      TrustlyScreen(
        uiState = uiState,
        navigateUp = {},
        connectingCardSucceeded = {},
        connectingCardFailed = {},
        connectingCardCancelled = {},
        retryConnectingCard = {},
        finishTrustlyFlow = {},
        changePaymentMethod = {},
      )
    }
  }
}

// Browsing is left out: it inflates the real Trustly WebView, which casts the local context to an
// Activity, and the preview's context is not one.
private class TrustlyUiStateProvider : CollectionPreviewParameterProvider<TrustlyUiState>(
  listOf(
    TrustlyUiState.Loading,
    TrustlyUiState.FailedToConnectCard,
    TrustlyUiState.CancelledConnectingCard,
    TrustlyUiState.FailedToStartSession(ErrorMessage("preview error message")),
    TrustlyUiState.SucceededInConnectingCard,
  ),
)
