package com.hedvig.android.feature.payin.account.ui.overview

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
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults
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
import com.hedvig.android.feature.payin.account.data.PayinAccount
import hedvig.resources.PAYMENTS_ACCOUNT
import hedvig.resources.PAYMENTS_INVOICE
import hedvig.resources.REFERRAL_PENDING_STATUS_LABEL
import hedvig.resources.Res
import hedvig.resources.swish
import hedvig.resources.trustly
import octopus.type.MemberPaymentProvider
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PayinAccountOverviewDestination(
  viewModel: PayinAccountOverviewViewModel,
  onConnectPayoutMethodClicked: () -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  PayinAccountOverviewScreen(
    uiState = uiState,
    onConnectPayoutMethodClicked = onConnectPayoutMethodClicked,
    onRetry = { viewModel.emit(PayinAccountOverviewEvent.Retry) },
    navigateUp = navigateUp,
  )
}

@Composable
private fun PayinAccountOverviewScreen(
  uiState: PayinAccountOverviewUiState,
  onConnectPayoutMethodClicked: () -> Unit,
  onRetry: () -> Unit,
  navigateUp: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = "Billing account", //todo!
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    when (uiState) {
      PayinAccountOverviewUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced(
          Modifier
            .weight(1f)
            .wrapContentHeight(),
        )
      }

      PayinAccountOverviewUiState.Error -> {
        HedvigErrorSection(
          onButtonClick = onRetry,
          modifier = Modifier
            .weight(1f)
            .wrapContentHeight(),
        )
      }

      is PayinAccountOverviewUiState.Content -> {
        PayoutAccountContent(
          currentMethod = uiState.currentMethod,
          availablePayinMethods = uiState.availablePayoutMethods,
          onConnectPayinMethodClicked = onConnectPayoutMethodClicked,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun PayoutAccountContent(
  currentMethod: PayinAccount?,
  availablePayinMethods: List<MemberPaymentProvider>,
  onConnectPayinMethodClicked: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Spacer(Modifier.height(8.dp))
    when (currentMethod) {
      null -> {
        if (availablePayinMethods.isNotEmpty()) {
          Spacer(Modifier.weight(1f))
          EmptyState(
            text = "You haven’t added a billing method yet. Add one to pay for your insurance.", //todo
            description = null,
            iconStyle = EmptyStateDefaults.EmptyStateIconStyle.INFO,
          )
        }
      }

      is PayinAccount.SwishPayin -> {
        val phoneNumber = currentMethod.phoneNumber.orEmpty()
        PayinAccountReadOnlyTextField(
          label = stringResource(Res.string.swish),
          text = if (currentMethod.isPending && phoneNumber.isBlank()) {
            stringResource(Res.string.REFERRAL_PENDING_STATUS_LABEL)
          } else {
            phoneNumber
          },
        )
      }

      is PayinAccount.Trustly -> {
        val accountNumber = formatBankAccountNumber(currentMethod.clearingNumber, currentMethod.accountNumber)
        PayinAccountReadOnlyTextField(
          label = formatBankAccountLabel(stringResource(Res.string.trustly), currentMethod.bankName),
          text = if (currentMethod.isPending && accountNumber.isBlank()) {
            stringResource(Res.string.REFERRAL_PENDING_STATUS_LABEL)
          } else {
            accountNumber
          },
        )
      }

      is PayinAccount.Invoice -> {
        PayinAccountReadOnlyTextField(
          stringResource(Res.string.PAYMENTS_ACCOUNT),
          stringResource(Res.string.PAYMENTS_INVOICE),
        )
      }
    }
    Spacer(Modifier.weight(1f))
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      if (currentMethod?.isPending == true) {
        HedvigNotificationCard(
          message = "You have just added or changed your billing method, it will appear here soon.", //todo
          priority = NotificationPriority.Info,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
      }
      if (availablePayinMethods.isNotEmpty()) {
        HedvigButton(
          text = if (currentMethod == null) {
            "Select billing method" //todo!
          } else {
            "Change billing method"  //todo!
          },
          onClick = onConnectPayinMethodClicked,
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
private fun PayinAccountReadOnlyTextField(label: String, text: String, modifier: Modifier = Modifier) {
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
private fun PreviewPayinAccountOverviewScreen(
  @PreviewParameter(PayinAccountOverviewUiStateProvider::class) uiState: PayinAccountOverviewUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      PayinAccountOverviewScreen(
        uiState = uiState,
        onConnectPayoutMethodClicked = {},
        onRetry = {},
        navigateUp = {},
      )
    }
  }
}

private class PayinAccountOverviewUiStateProvider : CollectionPreviewParameterProvider<PayinAccountOverviewUiState>(
  listOf(
    PayinAccountOverviewUiState.Loading,
    PayinAccountOverviewUiState.Error,
    PayinAccountOverviewUiState.Content(
      currentMethod = null,
      availablePayoutMethods = listOf(MemberPaymentProvider.SWISH, MemberPaymentProvider.TRUSTLY),
    ),
    PayinAccountOverviewUiState.Content(
      currentMethod = PayinAccount.SwishPayin(phoneNumber = "070-123 45 67", isPending = false),
      availablePayoutMethods = listOf(MemberPaymentProvider.SWISH),
    ),
    PayinAccountOverviewUiState.Content(
      currentMethod = PayinAccount.SwishPayin(phoneNumber = "070-123 45 67", isPending = false),
      availablePayoutMethods = listOf(MemberPaymentProvider.SWISH, MemberPaymentProvider.TRUSTLY),
    ),
    PayinAccountOverviewUiState.Content(
      currentMethod = PayinAccount.SwishPayin(phoneNumber = null, isPending = true),
      availablePayoutMethods = listOf(MemberPaymentProvider.SWISH),
    ),
    PayinAccountOverviewUiState.Content(
      currentMethod = PayinAccount.SwishPayin(phoneNumber = "070-123 45 67", isPending = true),
      availablePayoutMethods = listOf(MemberPaymentProvider.SWISH),
    ),
    PayinAccountOverviewUiState.Content(
      currentMethod = PayinAccount.Trustly(
        clearingNumber = "8327",
        accountNumber = "12345678",
        bankName = "Mock Swedbank",
        isPending = false,
      ),
      availablePayoutMethods = listOf(MemberPaymentProvider.TRUSTLY),
    ),
  ),
)
