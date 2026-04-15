package com.hedvig.android.feature.payoutaccount.ui.overview

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewUiState.Content
import octopus.type.MemberPaymentProvider

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
    topAppBarText = "Payout account",
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
) {
  Column {
    Spacer(Modifier.height(8.dp))
    when (currentMethod) {
      null -> {
        HedvigButton(
          text = "Connect payout account",
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
          textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Large,
          readOnly = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        if (availablePayoutMethods.size > 1) {
          Spacer(Modifier.height(8.dp))
          HedvigButton(
            text = "Change account",
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
          textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Large,
          readOnly = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        if (availablePayoutMethods.size > 1) {
          Spacer(Modifier.height(8.dp))
          HedvigButton(
            text = "Change account",
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
          labelText = "Account",
          textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Large,
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
