package com.hedvig.android.odyssey.step.singleitemcheckout

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.button.LargeContainedButton
import com.hedvig.android.core.designsystem.component.button.LargeOutlinedButton
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.UiMoney
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.model.FlowId
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import octopus.type.CurrencyCode
import kotlin.time.Duration.Companion.seconds

@Composable
internal fun PayoutScreen(
  uiState: PayoutUiState,
  exitFlow: () -> Unit,
  onDoneAfterPayout: (ClaimFlowStep) -> Unit,
  retryPayout: () -> Unit,
  openChat: () -> Unit,
) {
  BlurredFullScreenProgressOverlay {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .safeDrawingPadding()
        .padding(16.dp),
    ) {
      LoadingContent(uiState.status is PayoutUiState.Status.Loading)
      ErrorContent(
        show = uiState.status is PayoutUiState.Status.Error,
        allowInteraction = uiState.status is PayoutUiState.Status.Error,
        exitFlow = exitFlow,
        retryPayout = retryPayout,
        openChat = openChat,
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
  openChat: () -> Unit,
) {
  PoppingContent(
    show = show,
    modifier = Modifier.align(Alignment.Center),
  ) {
    Column(
      verticalArrangement = Arrangement.spacedBy(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
        painter = painterResource(com.hedvig.android.core.designsystem.R.drawable.ic_warning_triangle),
        contentDescription = null,
        Modifier.size(48.dp),
      )
      Text(
        text = stringResource(hedvig.resources.R.string.something_went_wrong),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineSmall,
      )
      Text(
        text = stringResource(hedvig.resources.R.string.NETWORK_ERROR_ALERT_MESSAGE),
        textAlign = TextAlign.Center,
        style = androidx.compose.material.MaterialTheme.typography.body1,
      )
    }
  }

  AnimatedVisibility(
    visible = show,
    enter = fadeIn(tween(600, 1_000, FastOutSlowInEasing)),
    exit = fadeOut(),
    modifier = Modifier.align(Alignment.BottomCenter),
  ) {
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      item {
        LargeOutlinedButton(
          onClick = openChat,
          enabled = allowInteraction,
        ) {
          Text(stringResource(hedvig.resources.R.string.open_chat))
        }
      }
      item {
        LargeOutlinedButton(
          onClick = exitFlow,
          enabled = allowInteraction,
        ) {
          Text(stringResource(hedvig.resources.R.string.general_close_button))
        }
      }
      item(span = { GridItemSpan(2) }) {
        LargeContainedButton(
          onClick = retryPayout,
          enabled = allowInteraction,
        ) {
          Text(stringResource(hedvig.resources.R.string.NETWORK_ERROR_ALERT_TRY_AGAIN_ACTION))
        }
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
    Text(
      text = stringResource(hedvig.resources.R.string.claims_payout_progress_title),
      style = MaterialTheme.typography.titleLarge,
    )
  }
}

@Composable
private fun BoxScope.PaidOutContent(
  status: PayoutUiState.Status,
  paidOutAmount: UiMoney,
  onDoneAfterPayout: (ClaimFlowStep) -> Unit,
) {
  PoppingContent(
    show = status is PayoutUiState.Status.PaidOut,
    modifier = Modifier.align(Alignment.Center),
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Icon(
        modifier = Modifier.size(48.dp),
        painter = painterResource(com.hedvig.android.odyssey.R.drawable.ic_check_circle),
        contentDescription = "Checkmark icon",
      )
      Spacer(Modifier.height(24.dp))
      Text(
        text = paidOutAmount.toString(),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.displaySmall,
      )
      Spacer(Modifier.height(16.dp))
      Text(
        text = stringResource(hedvig.resources.R.string.claims_payout_success_message),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleSmall,
      )
    }
  }

  AnimatedVisibility(
    visible = status is PayoutUiState.Status.PaidOut,
    enter = fadeIn(tween(600, 1_000, FastOutSlowInEasing)),
    exit = fadeOut(),
    modifier = Modifier.align(Alignment.BottomCenter),
  ) {
    LargeContainedButton(
      onClick = {
        if (status is PayoutUiState.Status.PaidOut) {
          onDoneAfterPayout(status.nextStep)
        }
      },
      enabled = status is PayoutUiState.Status.PaidOut,
    ) {
      Text(stringResource(hedvig.resources.R.string.claims_payout_done_label))
    }
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

// region Previews

@HedvigPreview
@Composable
private fun PreviewPayoutScreenLoading() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      PayoutScreen(
        PayoutUiState(UiMoney(1499.0, CurrencyCode.SEK), PayoutUiState.Status.Loading),
        {},
        {},
        {},
        {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewPayoutScreenError() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      PayoutScreen(
        PayoutUiState(UiMoney(1499.0, CurrencyCode.SEK), PayoutUiState.Status.Error),
        {},
        {},
        {},
        {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewPayoutScreenPaidOut() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      PayoutScreen(
        PayoutUiState(
          UiMoney(1499.0, CurrencyCode.SEK),
          PayoutUiState.Status.PaidOut(ClaimFlowStep.UnknownStep(FlowId(""))),
        ),
        {},
        {},
        {},
        {},
      )
    }
  }
}

@HedvigPreview
@Composable
private fun PreviewPayoutScreenAnimations() {
  val uiState by produceState(
    PayoutUiState(UiMoney(1499.0, CurrencyCode.SEK), PayoutUiState.Status.Loading),
  ) {
    while (isActive) {
      delay(2.seconds)
      value = value.copy(status = PayoutUiState.Status.PaidOut(ClaimFlowStep.UnknownStep(FlowId(""))))
      delay(2.seconds)
      value = value.copy(status = PayoutUiState.Status.Loading)
      delay(2.seconds)
      value = value.copy(status = PayoutUiState.Status.Error)
      delay(2.seconds)
      value = value.copy(status = PayoutUiState.Status.Loading)
    }
  }
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      PayoutScreen(uiState, {}, {}, {}, {})
    }
  }
}

// endregion
