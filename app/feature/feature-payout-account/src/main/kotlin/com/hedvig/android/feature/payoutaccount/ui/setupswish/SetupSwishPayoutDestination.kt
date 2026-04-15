package com.hedvig.android.feature.payoutaccount.ui.setupswish

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
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority

@Composable
internal fun SetupSwishPayoutDestination(
  viewModel: SetupSwishPayoutViewModel,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  SetupSwishPayoutScreen(
    uiState = uiState,
    onSave = { viewModel.emit(SetupSwishPayoutEvent.Save) },
    navigateUp = navigateUp,
  )
}

@Composable
private fun SetupSwishPayoutScreen(
  uiState: SetupSwishPayoutUiState,
  onSave: () -> Unit,
  navigateUp: () -> Unit,
) {
  LaunchedEffect(uiState.navigateBack) {
    if (uiState.navigateBack) navigateUp()
  }

  HedvigScaffold(
    topAppBarText = "Swish",
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    Spacer(Modifier.weight(1f))
    Column(Modifier.padding(horizontal = 16.dp)) {
      HedvigTextField(
        state = uiState.phoneNumberState,
        labelText = "Phone number",
        textFieldSize = HedvigTextFieldDefaults.TextFieldSize.Large,
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Phone,
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
      enabled = !uiState.isLoading && uiState.phoneNumberState.text.isNotBlank(),
      isLoading = uiState.isLoading,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    )
    Spacer(Modifier.height(16.dp))
  }
}
