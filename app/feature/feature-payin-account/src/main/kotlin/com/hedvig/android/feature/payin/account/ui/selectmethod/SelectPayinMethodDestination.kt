package com.hedvig.android.feature.payin.account.ui.selectmethod

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioOption
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.feature.payin.account.ui.components.PayinMethodHandoverIllustration
import com.hedvig.android.feature.payin.account.ui.components.PayinProviderPillow
import hedvig.resources.ONBOARDING_CONNECT_PAYMENT_BANK_LABEL
import hedvig.resources.PAYMENTS_INVOICE
import hedvig.resources.PAYMENT_CONNECT_SUBTITLE
import hedvig.resources.PAYMENT_CONNECT_TITLE
import hedvig.resources.PAYMENT_OPTION_CONNECTED_LABEL
import hedvig.resources.PAYMENT_OPTION_INVOICE_SUBTITLE
import hedvig.resources.PAYMENT_OPTION_SWISH_SUBTITLE
import hedvig.resources.PAYMENT_OPTION_TRUSTLY_SUBTITLE
import hedvig.resources.Res
import hedvig.resources.general_cancel_button
import hedvig.resources.swish
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SelectPayinMethodDestination(
  viewModel: SelectPayinMethodViewModel,
  onTrustlySelected: () -> Unit,
  onSwishSelected: () -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  SelectPayinMethodScreen(
    uiState = uiState,
    onProviderSelected = { viewModel.emit(SelectPayinMethodEvent.SelectProvider(it)) },
    onSubmitSelected = {
      when (uiState.selectedProvider) {
        PaymentProvider.Trustly -> {
          onTrustlySelected()
        }

        PaymentProvider.Swish -> {
          onSwishSelected()
        }

        else -> {}
      }
    },
    navigateUp = navigateUp,
  )
}

@Composable
private fun SelectPayinMethodScreen(
  uiState: SelectPayinMethodUiState,
  onProviderSelected: (PaymentProvider) -> Unit,
  onSubmitSelected: () -> Unit,
  navigateUp: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = null,
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    Spacer(Modifier.height(8.dp))
    FlowHeading(
      title = stringResource(Res.string.PAYMENT_CONNECT_TITLE),
      description = stringResource(Res.string.PAYMENT_CONNECT_SUBTITLE),
      baseStyle = HedvigTheme.typography.bodySmall,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    PayinMethodHandoverIllustration(
      uiState.selectedProvider,
      modifier = Modifier.align(Alignment.CenterHorizontally),
    )
    Spacer(Modifier.weight(1f))
    RadioGroup(
      options = uiState.availableProviders.mapNotNull {
        it.toRadioOption(
          uiState.currentProviders,
        )
      },
      selectedOption = uiState.selectedProvider?.let { RadioOptionId(it.rawValue) },
      onRadioOptionSelected = { option -> PaymentProvider.fromRawValue(option.id)?.let(onProviderSelected) },
      // Shown when the backend offers it, but there is nothing to hand the member over to: invoice
      // is set up for them rather than by them.
      disabledOptions = listOf(RadioOptionId(PaymentProvider.Invoice.rawValue)),
      optionIcon = { PayinProviderPillow(PaymentProvider.fromRawValue(it.id)) },
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      onClick = onSubmitSelected,
      enabled = uiState.selectedProvider != null,
      text = stringResource(Res.string.PAYMENT_CONNECT_TITLE),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      onClick = navigateUp,
      enabled = true,
      text = stringResource(Res.string.general_cancel_button),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}

/**
 * Null for providers this screen has no option for, so a value the backend adds later is left out
 * of the group.
 */
@Composable
private fun PaymentProvider.toRadioOption(currentProviders: List<PaymentProvider>): RadioOption? {
  val id = RadioOptionId(rawValue)
  return when (this) {
    PaymentProvider.Trustly -> RadioOption(
      id = id,
      text = stringResource(Res.string.ONBOARDING_CONNECT_PAYMENT_BANK_LABEL),
      label = if (currentProviders.contains(this)) {
        stringResource(Res.string.PAYMENT_OPTION_CONNECTED_LABEL)
      } else {
        stringResource(Res.string.PAYMENT_OPTION_TRUSTLY_SUBTITLE)
      },
    )

    PaymentProvider.Swish -> RadioOption(
      id = id,
      text = stringResource(Res.string.swish),
      label = if (currentProviders.contains(this)) {
        stringResource(Res.string.PAYMENT_OPTION_CONNECTED_LABEL)
      } else {
        stringResource(Res.string.PAYMENT_OPTION_SWISH_SUBTITLE)
      },
    )

    PaymentProvider.Invoice -> RadioOption(
      id = id,
      text = stringResource(Res.string.PAYMENTS_INVOICE),
      label = if (currentProviders.contains(this)) {
        stringResource(Res.string.PAYMENT_OPTION_CONNECTED_LABEL)
      } else {
        stringResource(Res.string.PAYMENT_OPTION_INVOICE_SUBTITLE)
      },
    )

    else -> null
  }
}

@Composable
@HedvigShortMultiScreenPreview
private fun PreviewSelectPayinMethodScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SelectPayinMethodScreen(
        uiState = SelectPayinMethodUiState(
          availableProviders = listOf(
            PaymentProvider.Swish,
            PaymentProvider.Invoice,
            PaymentProvider.Trustly,
          ),
          selectedProvider = PaymentProvider.Trustly,
          currentProviders = listOf(
            PaymentProvider.Swish,
          ),
        ),
        onProviderSelected = {},
        onSubmitSelected = {},
        navigateUp = {},
      )
    }
  }
}
