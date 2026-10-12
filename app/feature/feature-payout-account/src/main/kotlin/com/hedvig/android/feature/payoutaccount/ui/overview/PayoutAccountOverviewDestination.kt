package com.hedvig.android.feature.payoutaccount.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults.EmptyStateIconStyle
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigInformationSection
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.InfoInline
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount.BankAccount
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount.SwishPayout
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount.Trustly
import com.hedvig.android.feature.payoutaccount.ui.components.PayoutMethodRow
import com.hedvig.android.feature.payoutaccount.ui.components.PrimaryMethodLabel
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewEvent.Retry
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewUiState.Content
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewUiState.Error
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewUiState.Loading
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewUiState.NoPayoutOptions
import hedvig.resources.PAYMENT_CHOOSE_PRIMARY_BUTTON
import hedvig.resources.PAYMENT_METHOD_PENDING
import hedvig.resources.PAYOUT_ADD_PAYOUT_METHOD
import hedvig.resources.PAYOUT_MISSING_INFO
import hedvig.resources.PAYOUT_NO_PAYOUT_OPTIONS_SUBTITLE
import hedvig.resources.PAYOUT_NO_PAYOUT_OPTIONS_TITLE
import hedvig.resources.PAYOUT_PAGE_HEADING
import hedvig.resources.PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_BUTTON
import hedvig.resources.Res
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PayoutAccountOverviewDestination(
  viewModel: PayoutAccountOverviewViewModel,
  onConnectPayoutMethodClicked: () -> Unit,
  onChoosePrimaryMethodClicked: () -> Unit,
  navigateToTrustly: () -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var hasResumedOnce by rememberSaveable { mutableStateOf(false) }
  LifecycleResumeEffect(Unit) {
    if (hasResumedOnce) {
      viewModel.emit(PayoutAccountOverviewEvent.Refresh)
    } else {
      hasResumedOnce = true
    }
    onPauseOrDispose {}
  }
  PayoutAccountOverviewScreen(
    uiState = uiState,
    onConnectPayoutMethodClicked = onConnectPayoutMethodClicked,
    onChoosePrimaryMethodClicked = onChoosePrimaryMethodClicked,
    navigateToTrustly = navigateToTrustly,
    onRetry = { viewModel.emit(Retry) },
    navigateUp = navigateUp,
  )
}

@Composable
private fun PayoutAccountOverviewScreen(
  uiState: PayoutAccountOverviewUiState,
  onConnectPayoutMethodClicked: () -> Unit,
  onChoosePrimaryMethodClicked: () -> Unit,
  navigateToTrustly: () -> Unit,
  onRetry: () -> Unit,
  navigateUp: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = stringResource(Res.string.PAYOUT_PAGE_HEADING),
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
          title = stringResource(Res.string.PAYOUT_NO_PAYOUT_OPTIONS_TITLE),
          subTitle = stringResource(Res.string.PAYOUT_NO_PAYOUT_OPTIONS_SUBTITLE),
          buttonText = stringResource(Res.string.PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_BUTTON),
          onButtonClick = navigateToTrustly,
          modifier = Modifier
            .weight(1f)
            .wrapContentHeight(),
        )
      }

      is Content -> {
        PayoutAccountContent(
          currentMethods = uiState.currentMethods,
          availablePayoutMethods = uiState.availablePayoutMethods,
          onConnectPayoutMethodClicked = onConnectPayoutMethodClicked,
          onChoosePrimaryMethodClicked = onChoosePrimaryMethodClicked,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun PayoutAccountContent(
  currentMethods: List<PayoutAccount>,
  availablePayoutMethods: List<PaymentProvider>,
  onConnectPayoutMethodClicked: () -> Unit,
  onChoosePrimaryMethodClicked: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier
      .verticalScroll(rememberScrollState()),
  ) {
    Spacer(Modifier.height(8.dp))
    if (currentMethods.isEmpty()) {
      if (availablePayoutMethods.isNotEmpty()) {
        Spacer(Modifier.weight(1f))
        EmptyState(
          text = stringResource(Res.string.PAYOUT_MISSING_INFO),
          description = null,
          iconStyle = EmptyStateIconStyle.INFO,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
      }
    } else {
      Column(
        Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        if (currentMethods.any { it.isPending }) {
          HedvigNotificationCard(
            message = stringResource(Res.string.PAYMENT_METHOD_PENDING),
            priority = InfoInline,
            withIcon = true,
          )
        }
        for (method in currentMethods) {
          HedvigCard(
            shape = HedvigTheme.shapes.cornerLarge,
            modifier = Modifier.fillMaxWidth(),
          ) {
            PayoutMethodRow(
              method = method,
              modifier = Modifier.fillMaxWidth(),
              endSlot = if (method.isDefault) {
                { PrimaryMethodLabel() }
              } else {
                null
              },
            )
          }
        }
      }
    }
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    if (availablePayoutMethods.isNotEmpty()) {
      HedvigButton(
        text = stringResource(Res.string.PAYOUT_ADD_PAYOUT_METHOD),
        onClick = onConnectPayoutMethodClicked,
        enabled = true,
        buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
    }
    if (currentMethods.count { !it.isPending } > 1) {
      Spacer(Modifier.height(8.dp))
      HedvigTextButton(
        text = stringResource(Res.string.PAYMENT_CHOOSE_PRIMARY_BUTTON),
        onClick = onChoosePrimaryMethodClicked,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
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
        onChoosePrimaryMethodClicked = {},
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
      currentMethods = emptyList(),
      availablePayoutMethods = listOf(PaymentProvider.Swish, PaymentProvider.Trustly),
    ),
    Content(
      currentMethods = listOf(SwishPayout(phoneNumber = "070-123 45 67", isPending = false, isDefault = true)),
      availablePayoutMethods = listOf(PaymentProvider.Swish, PaymentProvider.Trustly),
    ),
    Content(
      currentMethods = listOf(SwishPayout(phoneNumber = null, isPending = true, isDefault = false)),
      availablePayoutMethods = listOf(PaymentProvider.Swish),
    ),
    Content(
      currentMethods = listOf(
        BankAccount(
          clearingNumber = "3300",
          accountNumber = "1234567",
          bankName = "Nordea",
          isPending = false,
          isDefault = false,
        ),
        SwishPayout(phoneNumber = "070-123 45 67", isPending = false, isDefault = true),
        Trustly(
          clearingNumber = "8327",
          accountNumber = "12345678",
          bankName = "Mock Swedbank",
          isPending = true,
          isDefault = false,
        ),
      ),
      availablePayoutMethods = listOf(PaymentProvider.Swish, PaymentProvider.Trustly, PaymentProvider.Nordea),
    ),
  ),
)
