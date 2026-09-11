package com.hedvig.android.feature.payin.account.ui.primary

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.hedvig.android.compose.ui.EmptyContentDescription
import com.hedvig.android.design.system.hedvig.HedvigBottomSheet
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HedvigTheme.colorScheme
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Error
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Info
import com.hedvig.android.design.system.hedvig.RadioGroup
import com.hedvig.android.design.system.hedvig.RadioOptionId
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.ThreeDotsLoading
import com.hedvig.android.design.system.hedvig.a11y.FlowHeading
import com.hedvig.android.design.system.hedvig.api.HedvigBottomSheetState
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.icon.HelipadFilled
import com.hedvig.android.design.system.hedvig.icon.HelipadOutline
import com.hedvig.android.design.system.hedvig.icon.Plus
import com.hedvig.android.design.system.hedvig.rememberHedvigBottomSheetState
import com.hedvig.android.feature.payin.account.data.InvoiceDelivery
import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.feature.payin.account.data.PayinAccount.Invoice
import com.hedvig.android.feature.payin.account.data.PayinAccount.SwishPayin
import com.hedvig.android.feature.payin.account.data.PayinAccount.Trustly
import com.hedvig.android.feature.payin.account.data.provider
import com.hedvig.android.feature.payin.account.ui.components.PayinMethodHandoverIllustration
import com.hedvig.android.feature.payin.account.ui.components.PayinMethodRow
import com.hedvig.android.feature.payin.account.ui.components.PayinProviderPillow
import com.hedvig.android.feature.payin.account.ui.components.PrimaryMethodLabel
import com.hedvig.android.feature.payin.account.ui.components.payinMethodTitle
import com.hedvig.android.feature.payin.account.ui.components.toRadioOption
import com.hedvig.android.feature.payin.account.ui.primary.SelectPrimaryPayinMethodEvent.ConfirmSelectedMethod
import com.hedvig.android.feature.payin.account.ui.primary.SelectPrimaryPayinMethodEvent.SelectMethod
import hedvig.resources.PAYMENT_CONNECT_SUBTITLE
import hedvig.resources.PAYMENT_CONNECT_TITLE
import hedvig.resources.PAYMENT_PRIMARY_CONFIRM_TITLE
import hedvig.resources.PAYMENT_PRIMARY_SUBTITLE
import hedvig.resources.PAYMENT_PRIMARY_TITLE
import hedvig.resources.Res
import hedvig.resources.Res.string
import hedvig.resources.general_cancel_button
import hedvig.resources.general_continue_button
import hedvig.resources.pillow_new_680
import octopus.type.MemberPaymentProvider
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SelectPrimaryPayinMethodDestination(
  viewModel: SelectPrimaryPayinMethodViewModel,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(uiState.hasSetPrimaryMethod) {
    if (uiState.hasSetPrimaryMethod) {
      navigateBack()
    }
  }
  SelectPrimaryPayinMethodScreen(
    uiState = uiState,
    onMethodSelected = { viewModel.emit(SelectMethod(it)) },
    onConfirm = { viewModel.emit(ConfirmSelectedMethod) },
    navigateUp = navigateUp,
    navigateBack = navigateBack,
  )
}

@Composable
private fun SelectPrimaryPayinMethodScreen(
  uiState: SelectPrimaryPayinMethodUiState,
  onMethodSelected: (PayinAccount) -> Unit,
  onConfirm: () -> Unit,
  navigateUp: () -> Unit,
  navigateBack: () -> Unit,
) {
  val confirmationSheetState = rememberHedvigBottomSheetState<PayinAccount>()
  HedvigScaffold(
    topAppBarText = null,
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    Spacer(Modifier.height(8.dp))
    FlowHeading(
      title = stringResource(Res.string.PAYMENT_PRIMARY_TITLE),
      description = stringResource(Res.string.PAYMENT_PRIMARY_SUBTITLE),
      baseStyle = HedvigTheme.typography.bodySmall,
      modifier = Modifier.padding(horizontal = 16.dp),
    )
    Spacer(Modifier.weight(1f))
    PayinMethodHandoverIllustration(
      provider = uiState.selectedMethod?.provider,
      modifier = Modifier
        .align(Alignment.CenterHorizontally),
    )
    Spacer(Modifier.weight(1f))
    RadioGroup(
      options = uiState.methods
        .sortedByDescending { it.isDefault }
        .map { it.toRadioOption() },
      disabledOptions = uiState.methods
        .filter { it.isDefault }
        .map { it.toRadioOption().id },
      selectedOption = uiState.selectedMethod?.let { RadioOptionId(it.provider.rawValue) },
      onRadioOptionSelected = { id ->
        val method = uiState.methods.firstOrNull { it.provider.rawValue == id.id }
        if (method != null) onMethodSelected(method)
      },
      optionIcon = { PayinProviderPillow(MemberPaymentProvider.safeValueOf(it.id)) },
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
      text = stringResource(string.general_continue_button),
      onClick = {
        val method = uiState.selectedMethod
        if (method != null) confirmationSheetState.show(method)
      },
      enabled = uiState.selectedMethod != null,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(8.dp))
    HedvigTextButton(
      text = stringResource(string.general_cancel_button),
      onClick = navigateBack,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
  ConfirmPrimaryPayinMethodBottomSheet(
    sheetState = confirmationSheetState,
    isConfirming = uiState.isConfirming,
    onConfirm = onConfirm,
  )
}

@Composable
private fun ConfirmPrimaryPayinMethodBottomSheet(
  sheetState: HedvigBottomSheetState<PayinAccount>,
  isConfirming: Boolean,
  onConfirm: () -> Unit,
) {
  HedvigBottomSheet(sheetState) { method ->
    ConfirmPrimaryPayinMethodBottomSheetContent(
      method = method,
      isConfirming = isConfirming,
      onConfirm = onConfirm,
      onDismiss = { sheetState.dismiss() },
    )
  }
}

@Composable
private fun ConfirmPrimaryPayinMethodBottomSheetContent(
  method: PayinAccount,
  isConfirming: Boolean,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
) {
  HedvigText(
    text = stringResource(string.PAYMENT_PRIMARY_CONFIRM_TITLE),
    textAlign = TextAlign.Center,
    modifier = Modifier.fillMaxWidth(),
  )
  Spacer(Modifier.height(24.dp))
  HedvigNotificationCard(
    // TODO: Add "Your next payment will be drawn from {method}. Claims payouts will also be sent to this
    //  account." / "Din nästa betalning dras från {method}. Skadeutbetalningar skickas också till detta konto."
    //  to Lokalise
    message = "Your next payment will be drawn from ${payinMethodTitle(method)}. " +
      "Claims payouts will also be sent to this account.",
    priority = Info,
    modifier = Modifier.fillMaxWidth(),
  )
  Spacer(Modifier.height(16.dp))
  Surface(
    shape = HedvigTheme.shapes.cornerLarge,
    color = colorScheme.surfacePrimary,
    modifier = Modifier.fillMaxWidth(),
  ) {
    PayinMethodRow(
      method = method,
      modifier = Modifier.fillMaxWidth(),
      endSlot = { PrimaryMethodLabel() },
    )
  }
  Spacer(Modifier.height(16.dp))
  HedvigButton(
    text = stringResource(string.general_continue_button),
    onClick = onConfirm,
    enabled = !isConfirming,
    isLoading = isConfirming,
    modifier = Modifier.fillMaxWidth(),
  )
  Spacer(Modifier.height(8.dp))
  HedvigTextButton(
    text = stringResource(string.general_cancel_button),
    onClick = onDismiss,
    modifier = Modifier.fillMaxWidth(),
  )
  Spacer(Modifier.height(16.dp))
}

@Composable
@HedvigShortMultiScreenPreview
private fun ConfirmPrimaryPayinMethodBottomSheetPreview() {
  HedvigTheme {
    Surface(color = colorScheme.backgroundPrimary) {
      Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      ) {
        ConfirmPrimaryPayinMethodBottomSheetContent(
          SwishPayin(
            "0709901232",
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
private fun PreviewSelectPrimaryPayinMethodScreen(
  @PreviewParameter(SelectedMethodIndexProvider::class) selectedMethod: Int?,
) {
  HedvigTheme {
    Surface(color = colorScheme.backgroundPrimary) {
      val methods = listOf(
        Trustly(
          "8327",
          "91234124",
          "Swedbank",
          isPending = false,
          isDefault = false,
        ),
        SwishPayin(
          "0709901232",
          isPending = false,
          isDefault = true,
        ),
        Invoice(
          delivery = InvoiceDelivery.Kivra,
          email = null,
          isPending = false,
          isDefault = false,
        ),
      )
      SelectPrimaryPayinMethodScreen(
        uiState = SelectPrimaryPayinMethodUiState(
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
      0,
      1,
      2,
    ),
  )
