package com.hedvig.android.feature.payoutaccount.ui.overview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewUiState.Content
import hedvig.resources.CHANGE_PAYOUT_METHOD_BUTTON_LABEL
import hedvig.resources.PAYMENTS_ACCOUNT
import hedvig.resources.PAYOUT_PAGE_HEADING
import hedvig.resources.PAYOUT_SELECT_PAYOUT_METHOD
import hedvig.resources.Res
import octopus.type.MemberPaymentProvider
import octopus.type.PaymentMethodInvoiceDelivery
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PayoutAccountOverviewDestination(
  viewModel: PayoutAccountOverviewViewModel,
  onConnectPayoutMethodClicked: () -> Unit,
  onEditBankAccountClicked: () -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  PayoutAccountOverviewScreen(
    uiState = uiState,
    onConnectPayoutMethodClicked = onConnectPayoutMethodClicked,
    onEditBankAccountClicked = onEditBankAccountClicked,
    onRetry = { viewModel.emit(PayoutAccountOverviewEvent.Retry) },
    navigateUp = navigateUp,
  )
}

@Composable
private fun PayoutAccountOverviewScreen(
  uiState: PayoutAccountOverviewUiState,
  onConnectPayoutMethodClicked: () -> Unit,
  onEditBankAccountClicked: () -> Unit,
  onRetry: () -> Unit,
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
          onEditBankAccountClicked = onEditBankAccountClicked,
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
  onEditBankAccountClicked: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Spacer(Modifier.height(8.dp))
    when (currentMethod) {
      null -> {
        Spacer(Modifier.weight(1f))
        HedvigButton(
          text = stringResource(Res.string.PAYOUT_SELECT_PAYOUT_METHOD),
          onClick = onConnectPayoutMethodClicked,
          enabled = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
      }

      is PayoutAccount.SwishPayout -> {
        HedvigTextField(
          text = currentMethod.phoneNumber,
          onValueChange = {},
          labelText = "Swish",
          textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
          readOnly = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        if (availablePayoutMethods.size > 1) {
          Spacer(Modifier.height(8.dp))
          HedvigButton(
            text = stringResource(Res.string.CHANGE_PAYOUT_METHOD_BUTTON_LABEL),
            onClick = onConnectPayoutMethodClicked,
            enabled = true,
            buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
        }
      }

      PayoutAccount.Trustly -> {
        HedvigTextField(
          text = "Trustly",
          onValueChange = {},
          labelText = "Account",
          textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
          readOnly = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        if (availablePayoutMethods.size > 1) {
          Spacer(Modifier.height(8.dp))
          HedvigButton(
            text = stringResource(Res.string.CHANGE_PAYOUT_METHOD_BUTTON_LABEL),
            onClick = onConnectPayoutMethodClicked,
            enabled = true,
            buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
        }
      }

      is PayoutAccount.Invoice -> {
        HedvigTextField(
          text = "Invoice",
          onValueChange = {},
          labelText = "Account",
          textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
          readOnly = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        if (availablePayoutMethods.size > 1) {
          Spacer(Modifier.height(8.dp))
          HedvigButton(
            text = stringResource(Res.string.CHANGE_PAYOUT_METHOD_BUTTON_LABEL),
            onClick = onConnectPayoutMethodClicked,
            enabled = true,
            buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
          )
        }
      }

      is PayoutAccount.BankAccount -> {
        val displayText = buildString {
          if (currentMethod.bankName != null) {
            append(currentMethod.bankName)
            append(" ")
          }
          append(currentMethod.clearingNumber)
          append("-")
          append(currentMethod.accountNumber)
        }
        HedvigTextField(
          text = displayText,
          onValueChange = {},
          labelText = stringResource(Res.string.PAYMENTS_ACCOUNT),
          textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
          readOnly = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(8.dp))
        HedvigButton(
          text = "Edit account",
          onClick = onEditBankAccountClicked,
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
@HedvigPreview
private fun PreviewPayoutAccountOverviewScreen(
  @PreviewParameter(PayoutAccountOverviewUiStateProvider::class) uiState: PayoutAccountOverviewUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PayoutAccountOverviewScreen(
        uiState = uiState,
        onConnectPayoutMethodClicked = {},
        onEditBankAccountClicked = {},
        onRetry = {},
        navigateUp = {},
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
      currentMethod = PayoutAccount.SwishPayout(phoneNumber = "070-123 45 67"),
      availablePayoutMethods = listOf(MemberPaymentProvider.SWISH),
    ),
    Content(
      currentMethod = PayoutAccount.SwishPayout(phoneNumber = "070-123 45 67"),
      availablePayoutMethods = listOf(MemberPaymentProvider.SWISH, MemberPaymentProvider.TRUSTLY),
    ),
    Content(
      currentMethod = PayoutAccount.Trustly,
      availablePayoutMethods = listOf(MemberPaymentProvider.TRUSTLY),
    ),
    Content(
      currentMethod = PayoutAccount.BankAccount(
        clearingNumber = "3300",
        accountNumber = "1234567",
        bankName = "Nordea",
      ),
      availablePayoutMethods = listOf(MemberPaymentProvider.NORDEA),
    ),
    Content(
      currentMethod = PayoutAccount.Invoice(
        delivery = PaymentMethodInvoiceDelivery.KIVRA,
        email = null,
      ),
      availablePayoutMethods = listOf(MemberPaymentProvider.INVOICE),
    ),
    Content(
      currentMethod = PayoutAccount.Invoice(
        delivery = PaymentMethodInvoiceDelivery.MAIL,
        email = "user@example.com",
      ),
      availablePayoutMethods = listOf(MemberPaymentProvider.INVOICE, MemberPaymentProvider.TRUSTLY),
    ),
  ),
)
