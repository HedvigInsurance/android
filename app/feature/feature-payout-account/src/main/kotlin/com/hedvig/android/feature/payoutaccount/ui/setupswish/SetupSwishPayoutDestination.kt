package com.hedvig.android.feature.payoutaccount.ui.setupswish

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
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.common.validation.PhoneNumberRules
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.payoutaccount.ui.components.PayoutSetupSuccessContent
import com.hedvig.android.ui.phonenumber.HedvigPhoneNumberField
import hedvig.resources.ODYSSEY_PHONE_NUMBER_LABEL
import hedvig.resources.PAYMENT_SWISH_SUCCESS_TITLE
import hedvig.resources.Res
import hedvig.resources.TIER_FLOW_COMMIT_PROCESSING_ERROR_DESCRIPTION
import hedvig.resources.general_save_button
import hedvig.resources.swish
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SetupSwishPayoutDestination(viewModel: SetupSwishPayoutViewModel, navigateUp: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  SetupSwishPayoutScreen(
    uiState = uiState,
    onSave = { viewModel.emit(SetupSwishPayoutEvent.Save) },
    onFinishSetup = { viewModel.emit(SetupSwishPayoutEvent.FinishSetup) },
    navigateUp = navigateUp,
  )
}

@Composable
private fun SetupSwishPayoutScreen(
  uiState: SetupSwishPayoutUiState,
  onSave: () -> Unit,
  onFinishSetup: () -> Unit,
  navigateUp: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = stringResource(Res.string.swish),
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    if (uiState.isConnected) {
      PayoutSetupSuccessContent(
        provider = PaymentProvider.Swish,
        title = stringResource(Res.string.PAYMENT_SWISH_SUCCESS_TITLE),
        onContinue = onFinishSetup,
      )
    } else {
      PhoneNumberContent(uiState = uiState, onSave = onSave)
    }
  }
}

@Composable
private fun ColumnScope.PhoneNumberContent(uiState: SetupSwishPayoutUiState, onSave: () -> Unit) {
  Spacer(Modifier.weight(1f))
  Column(Modifier.padding(horizontal = 16.dp)) {
    HedvigPhoneNumberField(
      state = uiState.phoneNumberState,
      labelText = stringResource(Res.string.ODYSSEY_PHONE_NUMBER_LABEL),
      rules = PhoneNumberRules.SwishPhoneNumber,
      modifier = Modifier.fillMaxWidth(),
    )
  }
  AnimatedVisibility(
    visible = uiState.errorMessage != null,
    enter = expandVertically(),
    exit = shrinkVertically(),
  ) {
    HedvigNotificationCard(
      message = uiState.errorMessage?.message
        ?: stringResource(Res.string.TIER_FLOW_COMMIT_PROCESSING_ERROR_DESCRIPTION),
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
    enabled = !uiState.isLoading &&
      PhoneNumberRules.SwishPhoneNumber.hasEnoughDigits(uiState.phoneNumberState.text),
    isLoading = uiState.isLoading,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(16.dp))
}

@Composable
@HedvigShortMultiScreenPreview
private fun PreviewSetupSwishPayoutScreenConnected(
  @PreviewParameter(BooleanCollectionPreviewParameterProvider::class) isConnected: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      SetupSwishPayoutScreen(
        uiState = SetupSwishPayoutUiState(
          phoneNumberState = TextFieldState(),
          isLoading = false,
          errorMessage = null,
          isConnected = isConnected,
        ),
        onSave = {},
        onFinishSetup = {},
        navigateUp = {},
      )
    }
  }
}
