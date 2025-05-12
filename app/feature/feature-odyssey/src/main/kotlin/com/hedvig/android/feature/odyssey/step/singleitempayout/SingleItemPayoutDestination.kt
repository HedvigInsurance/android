package com.hedvig.android.feature.odyssey.step.singleitempayout

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize.Medium
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle.Secondary
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigLinearProgressIndicator
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.getDescription
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
internal fun SingleItemPayoutDestination(
  viewModel: SingleItemPayoutViewModel,
  onDoneAfterPayout: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  closePayoutScreen: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  SingleItemPayoutScreen(
    uiState = uiState,
    retryPayout = viewModel::requestPayout,
    onDoneAfterPayout = onDoneAfterPayout,
    onNavigateToNewConversation = onNavigateToNewConversation,
    closePayoutScreen = closePayoutScreen,
  )
}

@Composable
private fun SingleItemPayoutScreen(
  uiState: PayoutUiState,
  retryPayout: () -> Unit,
  onDoneAfterPayout: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
  closePayoutScreen: () -> Unit,
) {
  Surface(
    color = HedvigTheme.colorScheme.backgroundPrimary,
    modifier = Modifier
      .fillMaxSize()
      .safeDrawingPadding(),
  ) {
    Box {
      LoadingContent(uiState.status is PayoutUiState.Status.Loading)
      ErrorContent(
        show = uiState.status is PayoutUiState.Status.Error,
        allowInteraction = uiState.status is PayoutUiState.Status.Error,
        exitFlow = closePayoutScreen,
        retryPayout = retryPayout,
        onNavigateToNewConversation = onNavigateToNewConversation,
      )
      PaidOutContent(
        status = uiState.status,
        paidOutAmount = uiState.amount,
        onDoneAfterPayout = onDoneAfterPayout,
      )
    }
  }
}

@Composable
private fun BoxScope.ErrorContent(
  show: Boolean,
  allowInteraction: Boolean,
  exitFlow: () -> Unit,
  retryPayout: () -> Unit,
  onNavigateToNewConversation: () -> Unit,
) {
  PoppingContent(
    show = show,
    modifier = Modifier.align(Alignment.Center),
  ) {
    EmptyState(
      text = stringResource(hedvig.resources.R.string.something_went_wrong),
      description = stringResource(hedvig.resources.R.string.NETWORK_ERROR_ALERT_MESSAGE),
      modifier = Modifier.fillMaxWidth(),
    )
  }

  AnimatedVisibility(
    visible = show,
    enter = fadeIn(tween(600, 1_000, FastOutSlowInEasing)),
    exit = fadeOut(),
    modifier = Modifier
      .align(Alignment.BottomCenter)
      .padding(16.dp),
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HedvigButton(
          text = stringResource(hedvig.resources.R.string.open_chat),
          onClick = onNavigateToNewConversation,
          enabled = allowInteraction,
          buttonStyle = Secondary,
          buttonSize = Medium,
          modifier = Modifier.weight(1f),
        )
        HedvigButton(
          text = stringResource(hedvig.resources.R.string.general_close_button),
          onClick = exitFlow,
          enabled = allowInteraction,
          buttonStyle = Secondary,
          buttonSize = Medium,
          modifier = Modifier.weight(1f),
        )
      }
      HedvigButton(
        text = stringResource(hedvig.resources.R.string.NETWORK_ERROR_ALERT_TRY_AGAIN_ACTION),
        onClick = retryPayout,
        enabled = allowInteraction,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Composable
private fun BoxScope.LoadingContent(show: Boolean) {
  PoppingContent(
    show = show,
    modifier = Modifier.align(Alignment.Center),
  ) {
    Column(
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(16.dp),
    ) {
      HedvigText(
        text = stringResource(hedvig.resources.R.string.claims_payout_progress_title),
        style = HedvigTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(24.dp))
      HedvigLinearProgressIndicator(
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 48.dp),
      )
    }
  }
}

@Composable
private fun BoxScope.PaidOutContent(
  status: PayoutUiState.Status,
  paidOutAmount: UiMoney,
  onDoneAfterPayout: () -> Unit,
) {
  PoppingContent(
    show = status is PayoutUiState.Status.PaidOut,
    modifier = Modifier.align(Alignment.Center),
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(16.dp),
    ) {
      val payoutVoiceover = paidOutAmount.getDescription()
      HedvigText(
        text = paidOutAmount.toString(),
        textAlign = TextAlign.Center,
        style = HedvigTheme.typography.displaySmall,
        modifier = Modifier.semantics {
          contentDescription = payoutVoiceover
        },
      )
      Spacer(Modifier.height(16.dp))
      HedvigText(
        text = stringResource(hedvig.resources.R.string.CLAIMS_PAYOUT_SUCCESS_LABEL),
        style = HedvigTheme.typography.bodySmall.copy(
          color = HedvigTheme.colorScheme.textSecondary,
          textAlign = TextAlign.Center,
          lineBreak = LineBreak.Heading,
        ),
      )
    }
  }

  AnimatedVisibility(
    visible = status is PayoutUiState.Status.PaidOut,
    enter = fadeIn(tween(600, 1_000, FastOutSlowInEasing)),
    exit = fadeOut(),
    modifier = Modifier
      .align(Alignment.BottomCenter)
      .padding(16.dp),
  ) {
    HedvigTextButton(
      text = stringResource(hedvig.resources.R.string.general_close_button),
      onClick = onDoneAfterPayout,
      enabled = status is PayoutUiState.Status.PaidOut,
      modifier = Modifier.fillMaxWidth(),
    )
  }
}

/**
 * A container to:
 * Pop the content in, by using an overshoot interpolator for scaling in
 * And scale the content up and fade it out as it exits the UI
 */
@Composable
private fun PoppingContent(show: Boolean, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
  AnimatedVisibility(
    visible = show,
    enter = fadeIn(tween(300, 300, FastOutSlowInEasing))
      .plus(scaleIn(animationSpec = tween(300, 300, OvershootEasing))),
    exit = fadeOut() + scaleOut(targetScale = 1.8f),
    modifier = modifier,
  ) {
    content()
  }
}

@Suppress("PrivatePropertyName")
private val OvershootEasing: Easing = Easing { fraction ->
  OvershootInterpolator().getInterpolation(fraction)
}

@HedvigPreview
@Composable
private fun PreviewPayoutScreenLoading(
  @PreviewParameter(PayoutUiStatePreviewProvider::class) payoutUiState: PayoutUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SingleItemPayoutScreen(
        payoutUiState,
        {},
        {},
        {},
        {},
      )
    }
  }
}

private class PayoutUiStatePreviewProvider() : CollectionPreviewParameterProvider<PayoutUiState>(
  listOf(
    PayoutUiState(UiMoney(1499.0, UiCurrencyCode.SEK), PayoutUiState.Status.Loading),
    PayoutUiState(UiMoney(1499.0, UiCurrencyCode.SEK), PayoutUiState.Status.Error),
    PayoutUiState(
      UiMoney(1499.0, UiCurrencyCode.SEK),
      PayoutUiState.Status.PaidOut,
    ),
  ),
)

@HedvigPreview
@Composable
private fun PreviewPayoutScreenAnimations() {
  val uiState by produceState(
    PayoutUiState(UiMoney(1499.0, UiCurrencyCode.SEK), PayoutUiState.Status.Loading),
  ) {
    while (isActive) {
      delay(2.seconds)
      value = value.copy(status = PayoutUiState.Status.PaidOut)
      delay(2.seconds)
      value = value.copy(status = PayoutUiState.Status.Error)
      delay(2.seconds)
      value = value.copy(status = PayoutUiState.Status.Loading)
    }
  }
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SingleItemPayoutScreen(uiState, {}, {}, {}, {})
    }
  }
}
