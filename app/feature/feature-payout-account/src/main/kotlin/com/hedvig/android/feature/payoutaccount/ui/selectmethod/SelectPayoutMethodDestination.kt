package com.hedvig.android.feature.payoutaccount.ui.selectmethod

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.hedvig.android.feature.payoutaccount.ui.components.PayoutMethodHandoverIllustration
import com.hedvig.android.feature.payoutaccount.ui.components.PayoutProviderPillow
import hedvig.resources.BANK_PAYOUT_METHOD_CARD_DESCRIPTION
import hedvig.resources.BANK_PAYOUT_METHOD_CARD_TITLE
import hedvig.resources.PAYOUT_METHOD_SWISH_DESCRIPTION
import hedvig.resources.PAYOUT_METHOD_TRUSTLY_DESCRIPTION
import hedvig.resources.PAYOUT_SELECT_PAYOUT_METHOD
import hedvig.resources.Res.string
import hedvig.resources.general_cancel_button
import hedvig.resources.general_continue_button
import hedvig.resources.swish
import hedvig.resources.trustly
import octopus.type.MemberPaymentProvider
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SelectPayoutMethodDestination(
  availableProviders: List<MemberPaymentProvider>,
  onTrustlySelected: () -> Unit,
  onNordeaSelected: () -> Unit,
  onSwishSelected: () -> Unit,
  navigateUp: () -> Unit,
) {
  // Held as the raw value so the choice survives process death without a saver of its own.
  var selectedRawValue by rememberSaveable { mutableStateOf<String?>(null) }
  val selectedProvider = selectedRawValue?.let { MemberPaymentProvider.safeValueOf(it) }
  SelectPayoutMethodScreen(
    availableProviders = availableProviders,
    selectedProvider = selectedProvider,
    onProviderSelected = { selectedRawValue = it.rawValue },
    onSubmitSelected = {
      when (selectedProvider) {
        MemberPaymentProvider.TRUSTLY -> {
          onTrustlySelected()
        }

        MemberPaymentProvider.NORDEA -> {
          onNordeaSelected()
        }

        MemberPaymentProvider.SWISH -> {
          onSwishSelected()
        }

        else -> {}
      }
    },
    navigateUp = navigateUp,
  )
}

@Composable
private fun SelectPayoutMethodScreen(
  availableProviders: List<MemberPaymentProvider>,
  selectedProvider: MemberPaymentProvider?,
  onProviderSelected: (MemberPaymentProvider) -> Unit,
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
      title = stringResource(string.PAYOUT_SELECT_PAYOUT_METHOD),
      description = null,
      baseStyle = HedvigTheme.typography.bodySmall,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    PayoutMethodHandoverIllustration(
      selectedProvider,
      modifier = Modifier.align(Alignment.CenterHorizontally),
    )
    Spacer(Modifier.weight(1f))
    RadioGroup(
      options = availableProviders.mapNotNull { it.toRadioOption() },
      selectedOption = selectedProvider?.let { RadioOptionId(it.rawValue) },
      onRadioOptionSelected = { onProviderSelected(MemberPaymentProvider.safeValueOf(it.id)) },
      optionIcon = { PayoutProviderPillow(MemberPaymentProvider.safeValueOf(it.id)) },
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      onClick = onSubmitSelected,
      enabled = selectedProvider != null,
      text = stringResource(string.general_continue_button),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      onClick = navigateUp,
      enabled = true,
      text = stringResource(string.general_cancel_button),
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
private fun MemberPaymentProvider.toRadioOption(): RadioOption? {
  val id = RadioOptionId(rawValue)
  return when (this) {
    MemberPaymentProvider.TRUSTLY -> RadioOption(
      id = id,
      text = stringResource(string.trustly),
      label = stringResource(string.PAYOUT_METHOD_TRUSTLY_DESCRIPTION),
    )

    MemberPaymentProvider.NORDEA -> RadioOption(
      id = id,
      text = stringResource(string.BANK_PAYOUT_METHOD_CARD_TITLE),
      label = stringResource(string.BANK_PAYOUT_METHOD_CARD_DESCRIPTION),
    )

    MemberPaymentProvider.SWISH -> RadioOption(
      id = id,
      text = stringResource(string.swish),
      label = stringResource(string.PAYOUT_METHOD_SWISH_DESCRIPTION),
    )

    else -> null
  }
}

@Composable
@HedvigShortMultiScreenPreview
private fun PreviewSelectPayoutMethodScreen() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SelectPayoutMethodScreen(
        availableProviders = listOf(
          MemberPaymentProvider.SWISH,
          MemberPaymentProvider.TRUSTLY,
          MemberPaymentProvider.NORDEA,
        ),
        selectedProvider = MemberPaymentProvider.SWISH,
        onProviderSelected = {},
        onSubmitSelected = {},
        navigateUp = {},
      )
    }
  }
}
