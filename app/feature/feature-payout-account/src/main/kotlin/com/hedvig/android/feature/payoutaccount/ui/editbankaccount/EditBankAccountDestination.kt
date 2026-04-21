package com.hedvig.android.feature.payoutaccount.ui.editbankaccount

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.GlobalSnackBarState
import hedvig.resources.BANK_PAYOUT_METHOD_CARD_TITLE
import hedvig.resources.BANK_PAYOUT_METHOD_FORM_CLEARING_FIELD_LABEL
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority

@Composable
internal fun EditBankAccountDestination(
  viewModel: EditBankAccountViewModel,
  globalSnackBarState: GlobalSnackBarState,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  EditBankAccountScreen(
    uiState = uiState,
    globalSnackBarState = globalSnackBarState,
    onSave = { viewModel.emit(EditBankAccountEvent.Save) },
    showedSnackBar = {
      viewModel.emit(EditBankAccountEvent.ShowedSnackBar)
      navigateUp()
    },
    navigateUp = navigateUp,
  )
}

@Composable
private fun EditBankAccountScreen(
  uiState: EditBankAccountUiState,
  globalSnackBarState: GlobalSnackBarState,
  onSave: () -> Unit,
  showedSnackBar: () -> Unit,
  navigateUp: () -> Unit,
) {
  LaunchedEffect(uiState.showSuccessSnackBar) {
    if (!uiState.showSuccessSnackBar) return@LaunchedEffect
    globalSnackBarState.show("Changes saved", NotificationPriority.Campaign)
    showedSnackBar()
  }

  HedvigScaffold(
    topAppBarText = stringResource(Res.string.BANK_PAYOUT_METHOD_CARD_TITLE),
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    Spacer(Modifier.weight(1f))
    Column(Modifier.padding(horizontal = 16.dp)) {
      HedvigTextField(
        state = uiState.clearingNumberState,
        labelText = stringResource(Res.string.BANK_PAYOUT_METHOD_FORM_CLEARING_FIELD_LABEL),
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Large,
        inputTransformation = uiState.clearingInputTransformation,
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
        ),
        trailingContent = uiState.bankName?.let {
          { HedvigText(text = it) }
        },
        modifier = Modifier.fillMaxWidth(),
      )
      Spacer(Modifier.height(4.dp))
      HedvigTextField(
        state = uiState.accountNumberState,
        labelText = "Account number",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Large,
        inputTransformation = uiState.accountNumberInputTransformation,
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
        ),
        modifier = Modifier.fillMaxWidth(),
      )
    }
    AnimatedVisibility(
      visible = uiState.errorMessage != null,
      enter = expandVertically(),
      exit = shrinkVertically(),
    ) {
      HedvigNotificationCard(
        message = uiState.errorMessage ?: "",
        priority = NotificationPriority.Attention,
        modifier = Modifier
          .padding(horizontal = 16.dp)
          .padding(top = 4.dp)
          .fillMaxWidth(),
      )
    }
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = "Save",
      onClick = onSave,
      enabled = !uiState.isLoading &&
        uiState.clearingNumberState.text.isNotBlank() &&
        uiState.accountNumberState.text.isNotBlank(),
      isLoading = uiState.isLoading,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}
