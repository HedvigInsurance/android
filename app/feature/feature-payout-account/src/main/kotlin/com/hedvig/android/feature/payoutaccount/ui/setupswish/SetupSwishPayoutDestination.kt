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
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.insert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.common.validation.PhoneNumberRules
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.design.system.hedvig.Checkbox
import com.hedvig.android.design.system.hedvig.CheckboxOption
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.LoadingState
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.RadioGroupDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.payoutaccount.ui.components.PayoutMethodHandoverIllustration
import com.hedvig.android.feature.payoutaccount.ui.components.PayoutSetupSuccessContent
import com.hedvig.android.ui.phonenumber.HedvigPhoneNumberField
import hedvig.resources.ODYSSEY_PHONE_NUMBER_LABEL
import hedvig.resources.PAYMENTS_ADD_SWISH_PAYOUT_INFO
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
    onToggleOwnNumberConfirmed = { viewModel.emit(SetupSwishPayoutEvent.ToggleOwnNumberConfirmed) },
    onFinishSetup = { viewModel.emit(SetupSwishPayoutEvent.FinishSetup) },
    navigateUp = navigateUp,
  )
}

@Composable
private fun SetupSwishPayoutScreen(
  uiState: SetupSwishPayoutUiState,
  onSave: () -> Unit,
  onToggleOwnNumberConfirmed: () -> Unit,
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
      PhoneNumberContent(
        uiState = uiState,
        onSave = onSave,
        onToggleOwnNumberConfirmed = onToggleOwnNumberConfirmed,
      )
    }
  }
}

@Composable
private fun ColumnScope.PhoneNumberContent(
  uiState: SetupSwishPayoutUiState,
  onSave: () -> Unit,
  onToggleOwnNumberConfirmed: () -> Unit,
) {
  Spacer(Modifier.weight(1f))
  PayoutMethodHandoverIllustration(
    PaymentProvider.Swish,
    modifier = Modifier.align(Alignment.CenterHorizontally),
    loadingState = LoadingState.PROCESSING,
    destinationBadge = null,
  )
  Spacer(Modifier.weight(1f))
  Column(Modifier.padding(horizontal = 16.dp)) {
    val maskColor = HedvigTheme.colorScheme.textTertiary
    HedvigPhoneNumberField(
      state = uiState.phoneNumberState,
      labelText = stringResource(Res.string.ODYSSEY_PHONE_NUMBER_LABEL),
      rules = PhoneNumberRules.SwishPhoneNumber,
      outputTransformation = remember(maskColor) { SwishPhoneNumberOutputTransformation(maskColor) },
      modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(4.dp))
    Checkbox(
      option = CheckboxOption(
        text = stringResource(Res.string.PAYMENTS_ADD_SWISH_PAYOUT_INFO),
      ),
      selected = uiState.isOwnNumberConfirmed,
      onCheckboxSelected = onToggleOwnNumberConfirmed,
      textStyle = HedvigTheme.typography.label,
      enabled = !uiState.isLoading,
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
      uiState.isOwnNumberConfirmed &&
      PhoneNumberRules.SwishPhoneNumber.hasEnoughDigits(uiState.phoneNumberState.text),
    isLoading = uiState.isLoading,
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
  )
  Spacer(Modifier.height(16.dp))
}

/**
 * Groups a domestic number as `070 123 45 67`, with the digits still to type shown as a placeholder in
 * [maskColor]. The grouping and the placeholder are built around a domestic number, and regrouping a
 * country-code one ("467…", "+467…") at the same positions reads as a different number altogether, so
 * those are shown exactly as they are held.
 */
private class SwishPhoneNumberOutputTransformation(private val maskColor: Color) : OutputTransformation {
  override fun TextFieldBuffer.transformOutput() {
    if (length > 0 && charAt(0) != '0') return
    for (separatorIndex in SeparatorIndices) {
      if (length > separatorIndex) insert(separatorIndex, " ")
    }
    val typedLength = length
    if (typedLength < Mask.length) {
      append(Mask.substring(typedLength))
      addStyle(SpanStyle(color = maskColor), typedLength, Mask.length)
    }
  }

  private companion object {
    const val Mask = "000 000 00 00"

    // Descending, so each insertion leaves the earlier indices pointing at the same digits.
    val SeparatorIndices = listOf(8, 6, 3)
  }
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
          isOwnNumberConfirmed = false,
          errorMessage = null,
          isConnected = isConnected,
        ),
        onSave = {},
        onToggleOwnNumberConfirmed = {},
        onFinishSetup = {},
        navigateUp = {},
      )
    }
  }
}
