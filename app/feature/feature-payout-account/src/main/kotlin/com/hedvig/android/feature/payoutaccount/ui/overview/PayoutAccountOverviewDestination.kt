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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigInformationSection
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults.TextFieldSize
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.Info
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount.BankAccount
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount.SwishPayout
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount.Trustly
import com.hedvig.android.feature.payoutaccount.ui.components.LockedPayoutMethodRow
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewEvent.Retry
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewUiState.Content
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewUiState.Error
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewUiState.Loading
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewUiState.NoPayoutOptions
import hedvig.resources.CHANGE_PAYOUT_METHOD_BUTTON_LABEL
import hedvig.resources.MY_PAYMENT_UPDATING_MESSAGE
import hedvig.resources.PAYMENTS_ACCOUNT
import hedvig.resources.PAYOUT_MISSING_INFO
import hedvig.resources.PAYOUT_NO_PAYOUT_OPTIONS_SUBTITLE
import hedvig.resources.PAYOUT_NO_PAYOUT_OPTIONS_TITLE
import hedvig.resources.PAYOUT_PAGE_HEADING
import hedvig.resources.PAYOUT_SELECT_PAYOUT_METHOD
import hedvig.resources.PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_BUTTON
import hedvig.resources.REFERRAL_PENDING_STATUS_LABEL
import hedvig.resources.Res
import hedvig.resources.Res.string
import hedvig.resources.swish
import hedvig.resources.trustly
import octopus.type.MemberPaymentProvider
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PayoutAccountOverviewDestination(
  viewModel: PayoutAccountOverviewViewModel,
  onConnectPayoutMethodClicked: () -> Unit,
  navigateToTrustly: () -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  PayoutAccountOverviewScreen(
    uiState = uiState,
    onConnectPayoutMethodClicked = onConnectPayoutMethodClicked,
    navigateToTrustly = navigateToTrustly,
    onRetry = { viewModel.emit(Retry) },
    navigateUp = navigateUp,
  )
}

@Composable
private fun PayoutAccountOverviewScreen(
  uiState: PayoutAccountOverviewUiState,
  onConnectPayoutMethodClicked: () -> Unit,
  navigateToTrustly: () -> Unit,
  onRetry: () -> Unit,
  navigateUp: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = stringResource(string.PAYOUT_PAGE_HEADING),
    navigateUp = navigateUp,
    modifier = Modifier.fillMaxSize(),
  ) {
    when (uiState) {
      Loading -> {
        HedvigFullScreenCenterAlignedProgressDebounced(
          Modifier
            .weight(1f)
            .wrapContentHeight(),
        )
      }

      Error -> {
        HedvigErrorSection(
          onButtonClick = onRetry,
          modifier = Modifier
            .weight(1f)
            .wrapContentHeight(),
        )
      }

      NoPayoutOptions -> {
        HedvigInformationSection(
          title = stringResource(string.PAYOUT_NO_PAYOUT_OPTIONS_TITLE),
          subTitle = stringResource(string.PAYOUT_NO_PAYOUT_OPTIONS_SUBTITLE),
          buttonText = stringResource(string.PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_BUTTON),
          onButtonClick = navigateToTrustly,
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
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Spacer(Modifier.height(8.dp))
    when (currentMethod) {
      null -> {
        if (availablePayoutMethods.isNotEmpty()) {
          Spacer(Modifier.weight(1f))
          EmptyState(
            text = stringResource(string.PAYOUT_MISSING_INFO),
            description = null,
            iconStyle = EmptyStateIconStyle.INFO,
          )
        }
      }

      is SwishPayout -> {
        val phoneNumber = currentMethod.phoneNumber.orEmpty()
        LockedPayoutMethodRow(
          provider = MemberPaymentProvider.SWISH,
          title = stringResource(string.swish),
          subtitle = if (currentMethod.isPending && phoneNumber.isBlank()) {
            stringResource(string.REFERRAL_PENDING_STATUS_LABEL)
          } else {
            phoneNumber
          },
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
      }

      is Trustly -> {
        val accountNumber = formatBankAccountNumber(currentMethod.clearingNumber, currentMethod.accountNumber)
        LockedPayoutMethodRow(
          title = formatBankAccountLabel(stringResource(string.trustly), currentMethod.bankName),
          subtitle = if (currentMethod.isPending && accountNumber.isBlank()) {
            stringResource(string.REFERRAL_PENDING_STATUS_LABEL)
          } else {
            accountNumber
          },
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          provider = MemberPaymentProvider.TRUSTLY,
        )
      }

      is BankAccount -> {
        val accountNumber = formatBankAccountNumber(currentMethod.clearingNumber, currentMethod.accountNumber)
        LockedPayoutMethodRow(
          title = formatBankAccountLabel(stringResource(string.PAYMENTS_ACCOUNT), currentMethod.bankName),
          subtitle = if (currentMethod.isPending && accountNumber.isBlank()) {
            stringResource(string.REFERRAL_PENDING_STATUS_LABEL)
          } else {
            accountNumber
          },
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          provider = MemberPaymentProvider.NORDEA,
        )
      }
    }
    Spacer(Modifier.weight(1f))
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      if (currentMethod?.isPending == true) {
        HedvigNotificationCard(
          message = stringResource(string.MY_PAYMENT_UPDATING_MESSAGE),
          priority = Info,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
      }
      if (availablePayoutMethods.isNotEmpty()) {
        HedvigButton(
          text = if (currentMethod == null) {
            stringResource(string.PAYOUT_SELECT_PAYOUT_METHOD)
          } else {
            stringResource(string.CHANGE_PAYOUT_METHOD_BUTTON_LABEL)
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
private fun PayoutAccountReadOnlyTextField(label: String, text: String, modifier: Modifier = Modifier) {
  HedvigTextField(
    text = text,
    onValueChange = {},
    labelText = label,
    textFieldSize = TextFieldSize.Medium,
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
        navigateToTrustly = {},
        onRetry = {},
        navigateUp = {},
      )
    }
  }
}

private class PayoutAccountOverviewUiStateProvider : CollectionPreviewParameterProvider<PayoutAccountOverviewUiState>(
  listOf(
    Loading,
    Error,
    NoPayoutOptions,
    Content(
      currentMethod = null,
      availablePayoutMethods = listOf(MemberPaymentProvider.SWISH, MemberPaymentProvider.TRUSTLY),
    ),
    Content(
      currentMethod = SwishPayout(phoneNumber = "070-123 45 67", isPending = false),
      availablePayoutMethods = listOf(MemberPaymentProvider.SWISH),
    ),
    Content(
      currentMethod = SwishPayout(phoneNumber = "070-123 45 67", isPending = false),
      availablePayoutMethods = listOf(MemberPaymentProvider.SWISH, MemberPaymentProvider.TRUSTLY),
    ),
    Content(
      currentMethod = SwishPayout(phoneNumber = null, isPending = true),
      availablePayoutMethods = listOf(MemberPaymentProvider.SWISH),
    ),
    Content(
      currentMethod = SwishPayout(phoneNumber = "070-123 45 67", isPending = true),
      availablePayoutMethods = listOf(MemberPaymentProvider.SWISH),
    ),
    Content(
      currentMethod = Trustly(
        clearingNumber = "8327",
        accountNumber = "12345678",
        bankName = "Mock Swedbank",
        isPending = false,
      ),
      availablePayoutMethods = listOf(MemberPaymentProvider.TRUSTLY),
    ),
    Content(
      currentMethod = BankAccount(
        clearingNumber = "3300",
        accountNumber = "1234567",
        bankName = "Nordea",
        isPending = false,
      ),
      availablePayoutMethods = listOf(MemberPaymentProvider.NORDEA),
    ),
    Content(
      currentMethod = BankAccount(
        clearingNumber = null,
        accountNumber = null,
        bankName = null,
        isPending = true,
      ),
      availablePayoutMethods = listOf(MemberPaymentProvider.NORDEA),
    ),
    Content(
      currentMethod = BankAccount(
        clearingNumber = "3300",
        accountNumber = "1234567",
        bankName = "Nordea",
        isPending = true,
      ),
      availablePayoutMethods = listOf(MemberPaymentProvider.NORDEA),
    ),
  ),
)
