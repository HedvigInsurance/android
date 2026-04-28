package com.hedvig.android.feature.payoutaccount.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewUiState.Content
import hedvig.resources.CHANGE_PAYOUT_METHOD_BUTTON_LABEL
import hedvig.resources.MY_PAYMENT_UPDATING_MESSAGE
import hedvig.resources.PAYMENTS_ACCOUNT
import hedvig.resources.PAYOUT_PAGE_HEADING
import hedvig.resources.PAYOUT_SELECT_PAYOUT_METHOD
import hedvig.resources.REFERRAL_PENDING_STATUS_LABEL
import hedvig.resources.Res
import hedvig.resources.general_back_button
import hedvig.resources.something_went_wrong
import octopus.type.MemberPaymentProvider
import octopus.type.PaymentMethodInvoiceDelivery
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PayoutAccountOverviewDestination(
  viewModel: PayoutAccountOverviewViewModel,
  onConnectPayoutMethodClicked: () -> Unit,
  navigateBack: () -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  PayoutAccountOverviewScreen(
    uiState = uiState,
    onConnectPayoutMethodClicked = onConnectPayoutMethodClicked,
    onRetry = { viewModel.emit(PayoutAccountOverviewEvent.Retry) },
    navigateBack = navigateBack,
    navigateUp = navigateUp,
  )
}

@Composable
private fun PayoutAccountOverviewScreen(
  uiState: PayoutAccountOverviewUiState,
  onConnectPayoutMethodClicked: () -> Unit,
  onRetry: () -> Unit,
  navigateBack: () -> Unit,
  navigateUp: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = stringResource(Res.string.PAYOUT_PAGE_HEADING),
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    when (uiState) {
      PayoutAccountOverviewUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced(
          Modifier
            .weight(1f)
            .wrapContentHeight(),
        )
      }

      PayoutAccountOverviewUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = onRetry,
          modifier = Modifier
            .weight(1f)
            .wrapContentHeight(),
        )
      }

      is Content -> {
        PayoutAccountContent(
          currentMethod = uiState.currentMethod,
          availablePayoutMethods = uiState.availablePayoutMethods,
          onConnectPayoutMethodClicked = onConnectPayoutMethodClicked,
          navigateBack = navigateBack,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun PayoutAccountContent(
  currentMethod: PayoutAccount?,
  availablePayoutMethods: List<MemberPaymentProvider>,
  onConnectPayoutMethodClicked: () -> Unit,
  navigateBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Spacer(Modifier.height(8.dp))
    when (currentMethod) {
      null -> {
        if (availablePayoutMethods.isEmpty()) {
          Spacer(Modifier.weight(1f))
          HedvigErrorSection(
            // todo copy when missing current and possible payout methods
            title = stringResource(Res.string.something_went_wrong),
            subTitle = null,
            buttonText = stringResource(Res.string.general_back_button),
            onButtonClick = navigateBack,
          )
        }
      }

      is PayoutAccount.SwishPayout -> {
        val phoneNumber = currentMethod.phoneNumber.orEmpty()
        PayoutAccountReadOnlyTextField(
          label = "Swish",
          text = if (currentMethod.isPending && phoneNumber.isBlank()) {
            stringResource(Res.string.REFERRAL_PENDING_STATUS_LABEL)
          } else {
            phoneNumber
          },
        )
      }

      is PayoutAccount.Trustly -> {
        val accountNumber = formatBankAccountNumber(currentMethod.clearingNumber, currentMethod.accountNumber)
        PayoutAccountReadOnlyTextField(
          label = formatBankAccountLabel("Trustly", currentMethod.bankName),
          text = if (currentMethod.isPending && accountNumber.isBlank()) {
            stringResource(Res.string.REFERRAL_PENDING_STATUS_LABEL)
          } else {
            accountNumber
          },
        )
      }

      is PayoutAccount.Invoice -> {
        PayoutAccountReadOnlyTextField(label = "Account", text = "Invoice")
      }

      is PayoutAccount.BankAccount -> {
        val accountNumber = formatBankAccountNumber(currentMethod.clearingNumber, currentMethod.accountNumber)
        PayoutAccountReadOnlyTextField(
          label = formatBankAccountLabel(stringResource(Res.string.PAYMENTS_ACCOUNT), currentMethod.bankName),
          text = if (currentMethod.isPending && accountNumber.isBlank()) {
            stringResource(Res.string.REFERRAL_PENDING_STATUS_LABEL)
          } else {
            accountNumber
          },
        )
      }
    }
    Spacer(Modifier.weight(1f))
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      if (currentMethod?.isPending == true) {
        HedvigNotificationCard(
          message = stringResource(Res.string.MY_PAYMENT_UPDATING_MESSAGE),
          priority = NotificationPriority.Info,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
      }
      if (availablePayoutMethods.isNotEmpty()) {
        HedvigButton(
          text = if (currentMethod == null) {
            stringResource(Res.string.PAYOUT_SELECT_PAYOUT_METHOD)
          } else {
            stringResource(Res.string.CHANGE_PAYOUT_METHOD_BUTTON_LABEL)
          },
          onClick = onConnectPayoutMethodClicked,
          enabled = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
      }
    }
    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun PayoutAccountReadOnlyTextField(
  label: String,
  text: String,
  modifier: Modifier = Modifier,
) {
  HedvigTextField(
    text = text,
    onValueChange = {},
    labelText = label,
    textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
    readOnly = true,
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
}

private fun formatBankAccountLabel(baseLabel: String, bankName: String?): String {
  return if (bankName != null) "$baseLabel - $bankName" else baseLabel
}

private fun formatBankAccountNumber(clearingNumber: String?, accountNumber: String?): String {
  return when {
    clearingNumber != null && accountNumber != null -> "$clearingNumber-$accountNumber"
    else -> clearingNumber.orEmpty()
  }
}

@Composable
@HedvigPreview
private fun PreviewPayoutAccountOverviewScreen(
  @PreviewParameter(PayoutAccountOverviewUiStateProvider::class) uiState: PayoutAccountOverviewUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PayoutAccountOverviewScreen(
        uiState = uiState,
        onConnectPayoutMethodClicked = {},
        onRetry = {},
        navigateUp = {},
        navigateBack = {},
      )
    }
  }
}

private class PayoutAccountOverviewUiStateProvider : CollectionPreviewParameterProvider<PayoutAccountOverviewUiState>(
  listOf(
    PayoutAccountOverviewUiState.Loading,
    PayoutAccountOverviewUiState.Error,
    Content(
      currentMethod = null,
      availablePayoutMethods = listOf(MemberPaymentProvider.SWISH, MemberPaymentProvider.TRUSTLY),
    ),
    Content(
      currentMethod = PayoutAccount.SwishPayout(phoneNumber = "070-123 45 67", isPending = false),
      availablePayoutMethods = listOf(MemberPaymentProvider.SWISH),
    ),
    Content(
      currentMethod = PayoutAccount.SwishPayout(phoneNumber = "070-123 45 67", isPending = false),
      availablePayoutMethods = listOf(MemberPaymentProvider.SWISH, MemberPaymentProvider.TRUSTLY),
    ),
    Content(
      currentMethod = PayoutAccount.SwishPayout(phoneNumber = null, isPending = true),
      availablePayoutMethods = listOf(MemberPaymentProvider.SWISH),
    ),
    Content(
      currentMethod = PayoutAccount.SwishPayout(phoneNumber = "070-123 45 67", isPending = true),
      availablePayoutMethods = listOf(MemberPaymentProvider.SWISH),
    ),
    Content(
      currentMethod = PayoutAccount.Trustly(
        clearingNumber = "8327",
        accountNumber = "12345678",
        bankName = "Mock Swedbank",
        isPending = false,
      ),
      availablePayoutMethods = listOf(MemberPaymentProvider.TRUSTLY),
    ),
    Content(
      currentMethod = PayoutAccount.BankAccount(
        clearingNumber = "3300",
        accountNumber = "1234567",
        bankName = "Nordea",
        isPending = false,
      ),
      availablePayoutMethods = listOf(MemberPaymentProvider.NORDEA),
    ),
    Content(
      currentMethod = PayoutAccount.BankAccount(
        clearingNumber = null,
        accountNumber = null,
        bankName = null,
        isPending = true,
      ),
      availablePayoutMethods = listOf(MemberPaymentProvider.NORDEA),
    ),
    Content(
      currentMethod = PayoutAccount.BankAccount(
        clearingNumber = "3300",
        accountNumber = "1234567",
        bankName = "Nordea",
        isPending = true,
      ),
      availablePayoutMethods = listOf(MemberPaymentProvider.NORDEA),
    ),
    Content(
      currentMethod = PayoutAccount.Invoice(
        delivery = PaymentMethodInvoiceDelivery.KIVRA,
        email = null,
        isPending = false,
      ),
      availablePayoutMethods = listOf(MemberPaymentProvider.INVOICE),
    ),
    Content(
      currentMethod = PayoutAccount.Invoice(
        delivery = PaymentMethodInvoiceDelivery.MAIL,
        email = "user@example.com",
        isPending = false,
      ),
      availablePayoutMethods = listOf(MemberPaymentProvider.INVOICE, MemberPaymentProvider.TRUSTLY),
    ),
  ),
)
