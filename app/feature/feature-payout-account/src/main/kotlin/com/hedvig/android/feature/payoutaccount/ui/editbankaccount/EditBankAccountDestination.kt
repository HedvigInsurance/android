package com.hedvig.android.feature.payoutaccount.ui.editbankaccount

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.payoutaccount.ui.components.PayoutSetupSuccessContent
import hedvig.resources.BANK_PAYOUT_METHOD_CARD_TITLE
import hedvig.resources.PAYMENTS_ACCOUNT
import hedvig.resources.PAYMENT_PAYOUT_BANK_SUCCESS_TITLE
import hedvig.resources.Res
import hedvig.resources.general_save_button
import hedvig.resources.something_went_wrong
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EditBankAccountDestination(viewModel: EditBankAccountViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  EditBankAccountScreen(
    uiState = uiState,
    onSave = { viewModel.emit(EditBankAccountEvent.Save) },
    onFinishSetup = { viewModel.emit(EditBankAccountEvent.FinishSetup) },
    navigateUp = navigateUp,
  )
}

@Composable
private fun EditBankAccountScreen(
  uiState: EditBankAccountUiState,
  onSave: () -> Unit,
  onFinishSetup: () -> Unit,
  navigateUp: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = stringResource(Res.string.BANK_PAYOUT_METHOD_CARD_TITLE),
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    if (uiState.isConnected) {
      PayoutSetupSuccessContent(
        provider = PaymentProvider.Nordea,
        title = stringResource(Res.string.PAYMENT_PAYOUT_BANK_SUCCESS_TITLE),
        onContinue = onFinishSetup,
      )
    } else {
      AccountNumberContent(uiState = uiState, onSave = onSave)
    }
  }
}

@Composable
private fun ColumnScope.AccountNumberContent(uiState: EditBankAccountUiState, onSave: () -> Unit) {
  Spacer(Modifier.weight(1f))
  Column(Modifier.padding(horizontal = 16.dp)) {
    HedvigTextField(
      state = uiState.accountNumberState,
      labelText = buildString {
        append(stringResource(Res.string.PAYMENTS_ACCOUNT))
        if (uiState.bankName != null) {
          append(" - ")
          append(uiState.bankName)
        }
      },
      textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Medium,
      inputTransformation = uiState.accountNumberInputTransformation,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      modifier = Modifier.fillMaxWidth(),
    )
  }
  AnimatedVisibility(
    visible = uiState.errorMessage != null,
    enter = expandVertically(),
    exit = shrinkVertically(),
  ) {
    HedvigNotificationCard(
      message = uiState.errorMessage?.ifBlank { stringResource(Res.string.something_went_wrong) }.orEmpty(),
      priority = NotificationPriority.Attention,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 4.dp)
        .fillMaxWidth(),
    )
  }
  Spacer(Modifier.height(16.dp))
  HedvigButton(
    text = stringResource(Res.string.general_save_button),
    onClick = onSave,
    enabled = uiState.canSave,
    isLoading = uiState.isLoading,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(16.dp))
}

@Composable
@HedvigShortMultiScreenPreview
private fun PreviewEditBankAccountScreenConnected() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      EditBankAccountScreen(
        uiState = EditBankAccountUiState(
          accountNumberState = TextFieldState(),
          bankName = "Swedbank",
          isLoading = false,
          errorMessage = null,
          isConnected = true,
        ),
        onSave = {},
        onFinishSetup = {},
        navigateUp = {},
      )
    }
  }
}
