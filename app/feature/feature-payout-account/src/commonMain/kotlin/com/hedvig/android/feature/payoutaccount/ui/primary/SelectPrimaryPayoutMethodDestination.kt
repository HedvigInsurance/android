package com.hedvig.android.feature.payoutaccount.ui.primary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Error
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Info
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.StaticRadioOption
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount.BankAccount
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount.SwishPayout
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount.Trustly
import com.hedvig.android.feature.payoutaccount.data.provider
import com.hedvig.android.feature.payoutaccount.ui.components.PayoutMethodHandoverIllustration
import com.hedvig.android.feature.payoutaccount.ui.components.PayoutMethodRow
import com.hedvig.android.feature.payoutaccount.ui.components.PayoutProviderPillow
import com.hedvig.android.feature.payoutaccount.ui.components.PrimaryMethodLabel
import com.hedvig.android.feature.payoutaccount.ui.components.payoutMethodTitle
import com.hedvig.android.feature.payoutaccount.ui.components.toRadioOption
import com.hedvig.android.feature.payoutaccount.ui.primary.SelectPrimaryPayoutMethodEvent.ConfirmSelectedMethod
import com.hedvig.android.feature.payoutaccount.ui.primary.SelectPrimaryPayoutMethodEvent.Retry
import com.hedvig.android.feature.payoutaccount.ui.primary.SelectPrimaryPayoutMethodEvent.SelectMethod
import hedvig.resources.PAYOUT_CHOOSE_METHOD_SUBTITLE
import hedvig.resources.PAYOUT_CONFIRMATION_INFO
import hedvig.resources.PAYOUT_CONFIRMATION_TITLE
import hedvig.resources.PAYOUT_SELECT_PAYOUT_METHOD
import hedvig.resources.Res
import hedvig.resources.general_cancel_button
import hedvig.resources.general_continue_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SelectPrimaryPayoutMethodDestination(
  viewModel: SelectPrimaryPayoutMethodViewModel,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val hasSetPrimaryMethod = (uiState as? SelectPrimaryPayoutMethodUiState.Content)?.hasSetPrimaryMethod == true
  LaunchedEffect(hasSetPrimaryMethod) {
    if (hasSetPrimaryMethod) {
      navigateBack()
    }
  }
  when (val state = uiState) {
    SelectPrimaryPayoutMethodUiState.Loading -> {
      HedvigScaffold(
        topAppBarText = null,
        navigateUp = navigateUp,
        modifier = Modifier.fillMaxSize(),
      ) {
        HedvigFullScreenCenterAlignedProgressDebounced(
          Modifier
            .weight(1f)
            .wrapContentHeight(),
        )
      }
    }

    SelectPrimaryPayoutMethodUiState.Failed -> {
      HedvigScaffold(
        topAppBarText = null,
        navigateUp = navigateUp,
        modifier = Modifier.fillMaxSize(),
      ) {
        HedvigErrorSection(
          onButtonClick = { viewModel.emit(Retry) },
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .weight(1f)
            .wrapContentHeight(),
        )
      }
    }

    is SelectPrimaryPayoutMethodUiState.Content -> {
      SelectPrimaryPayoutMethodScreen(
        uiState = state,
        onMethodSelected = { viewModel.emit(SelectMethod(it)) },
        onConfirm = { viewModel.emit(ConfirmSelectedMethod) },
        navigateUp = navigateUp,
        navigateBack = navigateBack,
      )
    }
  }
}

@Composable
private fun SelectPrimaryPayoutMethodScreen(
  uiState: SelectPrimaryPayoutMethodUiState.Content,
  onMethodSelected: (PayoutAccount) -> Unit,
  onConfirm: () -> Unit,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
) {
  val confirmationSheetState = rememberHedvigBottomSheetState<PayoutAccount>()
  HedvigScaffold(
    topAppBarText = null,
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    Spacer(Modifier.height(8.dp))
    FlowHeading(
      title = stringResource(Res.string.PAYOUT_SELECT_PAYOUT_METHOD),
      description = stringResource(Res.string.PAYOUT_CHOOSE_METHOD_SUBTITLE),
      baseStyle = HedvigTheme.typography.bodySmall,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    PayoutMethodHandoverIllustration(
      provider = uiState.selectedMethod?.provider,
      modifier = Modifier.align(Alignment.CenterHorizontally),
    )
    Spacer(Modifier.weight(1f))
    val currentDefault = uiState.methods.firstOrNull { it.isDefault }
    if (currentDefault != null) {
      StaticRadioOption(
        option = currentDefault.toRadioOption(),
        trailingContent = { PrimaryMethodLabel() },
        optionIcon = { PayoutProviderPillow(currentDefault.provider) },
        modifier = Modifier.padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(4.dp))
    }
    RadioGroup(
      options = uiState.methods
        .filter { !it.isPending }
        .filter { !it.isDefault }
        .map { it.toRadioOption() },
      selectedOption = uiState.selectedMethod?.let { RadioOptionId(it.provider.rawValue) },
      onRadioOptionSelected = { id ->
        val method = uiState.methods.firstOrNull { it.provider.rawValue == id.id }
        if (method != null) onMethodSelected(method)
      },
      optionIcon = { PayoutProviderPillow(PaymentProvider.fromRawValue(it.id)) },
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    if (uiState.errorMessage != null) {
      Spacer(Modifier.height(8.dp))
      HedvigNotificationCard(
        message = uiState.errorMessage,
        priority = Error,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(Res.string.general_continue_button),
      onClick = {
        val method = uiState.selectedMethod
        if (method != null) confirmationSheetState.show(method)
      },
      enabled = uiState.selectedMethod != null &&
        !uiState.selectedMethod.isDefault,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(Res.string.general_cancel_button),
      onClick = navigateBack,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
  ConfirmPrimaryPayoutMethodBottomSheet(
    sheetState = confirmationSheetState,
    isConfirming = uiState.isConfirming,
    onConfirm = onConfirm,
  )
}

@Composable
private fun ConfirmPrimaryPayoutMethodBottomSheet(
  sheetState: HedvigBottomSheetState<PayoutAccount>,
  isConfirming: Boolean,
  onConfirm: () -> Unit,
) {
  HedvigBottomSheet(sheetState) { method ->
    ConfirmPrimaryPayoutMethodBottomSheetContent(
      method = method,
      isConfirming = isConfirming,
      onConfirm = onConfirm,
      onDismiss = { sheetState.dismiss() },
    )
  }
}

@Composable
private fun ConfirmPrimaryPayoutMethodBottomSheetContent(
  method: PayoutAccount,
  isConfirming: Boolean,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  HedvigText(
    text = stringResource(Res.string.PAYOUT_CONFIRMATION_TITLE),
    textAlign = TextAlign.Center,
    modifier = Modifier.fillMaxWidth(),
  )
  Spacer(Modifier.height(24.dp))
  HedvigNotificationCard(
    message = stringResource(Res.string.PAYOUT_CONFIRMATION_INFO, payoutMethodTitle(method)),
    priority = Info,
    modifier = Modifier.fillMaxWidth(),
  )
  Spacer(Modifier.height(16.dp))
  Surface(
    shape = HedvigTheme.shapes.cornerLarge,
    color = HedvigTheme.colorScheme.surfacePrimary,
    modifier = Modifier.fillMaxWidth(),
  ) {
    PayoutMethodRow(
      method = method,
      modifier = Modifier.fillMaxWidth(),
      endSlot = { PrimaryMethodLabel() },
    )
  }
  Spacer(Modifier.height(16.dp))
  HedvigButton(
    text = stringResource(Res.string.general_continue_button),
    onClick = onConfirm,
    enabled = !isConfirming,
    isLoading = isConfirming,
    modifier = Modifier.fillMaxWidth(),
  )
  Spacer(Modifier.height(8.dp))
  HedvigTextButton(
    text = stringResource(Res.string.general_cancel_button),
    onClick = onDismiss,
    modifier = Modifier.fillMaxWidth(),
  )
  Spacer(Modifier.height(16.dp))
}

@Composable
@HedvigShortMultiScreenPreview
private fun ConfirmPrimaryPayoutMethodBottomSheetPreview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      ) {
        ConfirmPrimaryPayoutMethodBottomSheetContent(
          SwishPayout(
            "070-990 12 32",
            isPending = false,
            isDefault = false,
          ),
          isConfirming = false,
          onConfirm = {},
          onDismiss = {},
        )
      }
    }
  }
}

@Composable
@HedvigShortMultiScreenPreview
private fun PreviewSelectPrimaryPayoutMethodScreen(
  @PreviewParameter(SelectedMethodIndexProvider::class) selectedMethod: Int?,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      val methods = listOf(
        Trustly(
          "8327",
          "91234124",
          "Swedbank",
          isPending = false,
          isDefault = true,
        ),
        SwishPayout(
          "070-990 12 32",
          isPending = false,
          isDefault = false,
        ),
        BankAccount(
          "3300",
          "1234567",
          "Nordea",
          isPending = false,
          isDefault = false,
        ),
      )
      SelectPrimaryPayoutMethodScreen(
        uiState = SelectPrimaryPayoutMethodUiState.Content(
          methods = methods,
          selectedMethod = selectedMethod?.let { methods[it] },
        ),
        onMethodSelected = {},
        onConfirm = {},
        navigateUp = {},
        navigateBack = {},
      )
    }
  }
}

private class SelectedMethodIndexProvider :
  CollectionPreviewParameterProvider<Int?>(
    listOf(
      null,
      1,
      2,
    ),
  )
