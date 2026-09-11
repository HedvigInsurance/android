package com.hedvig.android.feature.payin.account.ui.setupswish

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.PaymentMethodHandoverIllustration
import com.hedvig.android.design.system.hedvig.PaymentMethodMarkSize
import com.hedvig.android.design.system.hedvig.PaymentMethodTileBadge
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.icon.ArrowNorthEast
import com.hedvig.android.design.system.hedvig.icon.Checkmark
import com.hedvig.android.design.system.hedvig.icon.Close
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.colored.Swish
import hedvig.resources.GENERAL_RETRY
import hedvig.resources.PAYMENT_CHANGE_METHOD_BUTTON
import hedvig.resources.PAYMENT_OPEN_SWISH_BUTTON
import hedvig.resources.PAYMENT_SWISH_APPROVE_TITLE
import hedvig.resources.PAYMENT_SWISH_FAILURE_TITLE
import hedvig.resources.PAYMENT_SWISH_SUCCESS_SUBTITLE
import hedvig.resources.PAYMENT_SWISH_SUCCESS_TITLE
import hedvig.resources.Res
import hedvig.resources.general_cancel_button
import hedvig.resources.general_continue_button
import hedvig.resources.something_went_wrong
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SwishPayinStatusDestination(
  viewModel: SwishPayinStatusViewModel,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
  finishSwishSetup: () -> Unit,
  changePaymentMethod: () -> Unit,
  openUrl: (String) -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  SwishPayinStatusScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    onCancel = navigateBack,
    onContinue = finishSwishSetup,
    onRetry = { viewModel.emit(SwishPayinStatusEvent.Retry) },
    onChangePaymentMethod = changePaymentMethod,
    openUrl = openUrl,
  )
}

@Composable
private fun SwishPayinStatusScreen(
  uiState: SwishPayinStatusUiState,
  navigateUp: () -> Unit,
  onCancel: () -> Unit,
  onContinue: () -> Unit,
  onRetry: () -> Unit,
  onChangePaymentMethod: () -> Unit,
  openUrl: (String) -> Unit,
) {
  HedvigScaffold(
    topAppBarText = null,
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    Spacer(Modifier.height(8.dp))
    FlowHeading(
      title = when (uiState) {
        is SwishPayinStatusUiState.PendingApproval -> stringResource(Res.string.PAYMENT_SWISH_APPROVE_TITLE)
        SwishPayinStatusUiState.Connected -> stringResource(Res.string.PAYMENT_SWISH_SUCCESS_TITLE)
        is SwishPayinStatusUiState.Failed -> stringResource(Res.string.PAYMENT_SWISH_FAILURE_TITLE)
      },
      description = when (uiState) {
        is SwishPayinStatusUiState.PendingApproval -> null
        SwishPayinStatusUiState.Connected -> stringResource(Res.string.PAYMENT_SWISH_SUCCESS_SUBTITLE)
        is SwishPayinStatusUiState.Failed -> uiState.message ?: stringResource(Res.string.something_went_wrong)
      },
      baseStyle = HedvigTheme.typography.bodySmall,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    PaymentMethodHandoverIllustration(
      modifier = Modifier.align(Alignment.CenterHorizontally),
      destinationBadge = { SetupStatusBadge(uiState) },
      mark = { Image(HedvigIcons.Swish, EmptyContentDescription, Modifier.size(PaymentMethodMarkSize)) },
    )
    Spacer(Modifier.weight(1f))
    when (uiState) {
      is SwishPayinStatusUiState.PendingApproval -> {
        HedvigButton(
          text = stringResource(Res.string.PAYMENT_OPEN_SWISH_BUTTON),
          onClick = { openUrl(uiState.redirectUrl) },
          enabled = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))
        HedvigTextButton(
          text = stringResource(Res.string.general_cancel_button),
          onClick = onCancel,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
      }

      SwishPayinStatusUiState.Connected -> {
        HedvigText(
          // TODO: Add "You can change payment method later" / "Du kan byta betalningsmetod senare"
          //  to Lokalise
          text = "You can change payment method later",
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
          onClick = onContinue,
          enabled = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
      }

      is SwishPayinStatusUiState.Failed -> {
        HedvigButton(
          text = stringResource(Res.string.GENERAL_RETRY),
          onClick = onRetry,
          enabled = !uiState.isRetrying,
          isLoading = uiState.isRetrying,
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
    Spacer(Modifier.height(16.dp))
  }
}

/** Marks the Hedvig symbol with where the setup has got to: handed over, landed, or rejected. */
@Composable
private fun SetupStatusBadge(uiState: SwishPayinStatusUiState) {
  when (uiState) {
    is SwishPayinStatusUiState.PendingApproval -> PaymentMethodTileBadge(
      icon = HedvigIcons.ArrowNorthEast,
      containerColor = HedvigTheme.colorScheme.signalBlueElement,
      contentColor = HedvigTheme.colorScheme.fillWhite,
    )

    SwishPayinStatusUiState.Connected -> PaymentMethodTileBadge(
      icon = HedvigIcons.Checkmark,
      containerColor = HedvigTheme.colorScheme.signalGreenElement,
      contentColor = HedvigTheme.colorScheme.fillWhite,
    )

    is SwishPayinStatusUiState.Failed -> PaymentMethodTileBadge(
      icon = HedvigIcons.Close,
      containerColor = HedvigTheme.colorScheme.signalRedElement,
      contentColor = HedvigTheme.colorScheme.fillWhite,
    )
  }
}

@Composable
@HedvigShortMultiScreenPreview
private fun PreviewSwishPayinStatusScreen(
  @PreviewParameter(SwishPayinStatusUiStateProvider::class) uiState: SwishPayinStatusUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SwishPayinStatusScreen(
        uiState = uiState,
        navigateUp = {},
        onCancel = {},
        onContinue = {},
        onRetry = {},
        onChangePaymentMethod = {},
        openUrl = {},
      )
    }
  }
}

private class SwishPayinStatusUiStateProvider : CollectionPreviewParameterProvider<SwishPayinStatusUiState>(
  listOf(
    SwishPayinStatusUiState.PendingApproval("https://swish"),
    SwishPayinStatusUiState.Connected,
    SwishPayinStatusUiState.Failed(null),
    SwishPayinStatusUiState.Failed("The connection was declined in the Swish app", isRetrying = true),
  ),
)
