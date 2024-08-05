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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.button.HedvigOutlinedButton
import com.hedvig.android.core.designsystem.component.button.HedvigTextButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
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
    color = MaterialTheme.colorScheme.background,
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
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(16.dp),
    ) {
      Text(
        text = stringResource(hedvig.resources.R.string.something_went_wrong),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineSmall,
      )
      Text(
        text = stringResource(hedvig.resources.R.string.NETWORK_ERROR_ALERT_MESSAGE),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
      )
    }
  }

  AnimatedVisibility(
    visible = show,
    enter = fadeIn(tween(600, 1_000, FastOutSlowInEasing)),
    exit = fadeOut(),
    modifier = Modifier
      .align(Alignment.BottomCenter)
      .padding(16.dp),
  ) {
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      item {
        HedvigOutlinedButton(
          onClick = onNavigateToNewConversation,
          enabled = allowInteraction,
        ) {
          Text(stringResource(hedvig.resources.R.string.open_chat))
        }
      }
      item {
        HedvigOutlinedButton(
          onClick = exitFlow,
          enabled = allowInteraction,
        ) {
          Text(stringResource(hedvig.resources.R.string.general_close_button))
        }
      }
      item(span = { GridItemSpan(2) }) {
        HedvigContainedButton(
          text = stringResource(hedvig.resources.R.string.NETWORK_ERROR_ALERT_TRY_AGAIN_ACTION),
          onClick = retryPayout,
          enabled = allowInteraction,
        )
      }
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
      Text(
        text = stringResource(hedvig.resources.R.string.claims_payout_progress_title),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(24.dp))
      LinearProgressIndicator(
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
      Text(
        text = paidOutAmount.toString(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.displayMedium,
      )
      Spacer(Modifier.height(16.dp))
      Text(
        text = stringResource(hedvig.resources.R.string.CLAIMS_PAYOUT_SUCCESS_LABEL),
        style = MaterialTheme.typography.bodyLarge.copy(
          color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    Surface(color = MaterialTheme.colorScheme.background) {
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
    PayoutUiState(UiMoney(1499.0, UiCurrencyCode.SEK), PayoutUiState.Status.NotStarted),
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
    Surface(color = MaterialTheme.colorScheme.background) {
      SingleItemPayoutScreen(uiState, {}, {}, {}, {})
    }
  }
}
