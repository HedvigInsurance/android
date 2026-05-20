package com.hedvig.android.feature.payin.account.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextField
import com.hedvig.android.design.system.hedvig.HedvigTextFieldDefaults
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
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
  onTrustlySelected: () -> Unit,
  onSwishSelected: () -> Unit,
  onInvoiceSelected: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  PayinAccountOverviewScreen(
    uiState = uiState,
    onConnectPayoutMethodClicked = onConnectPayoutMethodClicked,
    onRetry = { viewModel.emit(PayinAccountOverviewEvent.Retry) },
    navigateUp = navigateUp,
    onTrustlySelected = onTrustlySelected,
    onSwishSelected = onSwishSelected,
    onInvoiceSelected = onInvoiceSelected
  )
}

@Composable
private fun PayinAccountOverviewScreen(
  uiState: PayinAccountOverviewUiState,
  onConnectPayoutMethodClicked: () -> Unit,
  onRetry: () -> Unit,
  navigateUp: () -> Unit,
  onTrustlySelected: () -> Unit,
  onSwishSelected: () -> Unit,
  onInvoiceSelected: () -> Unit,
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
          currentMethods = uiState.currentMethods,
          availablePayinMethods = uiState.availablePayinMethods,
          onConnectPayinMethodClicked = onConnectPayoutMethodClicked,
          modifier = Modifier.weight(1f),
          onTrustlySelected = onTrustlySelected,
          onSwishSelected = onSwishSelected,
          onInvoiceSelected = onInvoiceSelected
        )
      }
    }
  }
}

@Composable
private fun PayoutAccountContent(
  currentMethods: List<PayinAccount>,
  availablePayinMethods: List<MemberPaymentProvider>,
  onConnectPayinMethodClicked: () -> Unit,
  onTrustlySelected: () -> Unit,
  onSwishSelected: () -> Unit,
  onInvoiceSelected: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Spacer(Modifier.height(8.dp))
    if (currentMethods.isEmpty()) {
      if (availablePayinMethods.isNotEmpty()) {
        Spacer(Modifier.weight(1f))
        EmptyState(
          text = "You haven’t added a billing method yet. Add one to pay for your insurance.", //todo
          description = null,
          iconStyle = EmptyStateDefaults.EmptyStateIconStyle.INFO,
        )
      }
    } else {
      HedvigText("Active billing methods", modifier = Modifier.padding(horizontal = 16.dp)) //todo
      Spacer(Modifier.height(16.dp))
      Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        currentMethods.forEach { method ->
          when (method) {
            is PayinAccount.SwishPayin -> {
              val phoneNumber = method.phoneNumber.orEmpty()
              CurrentPayinMethodRow(
                label = stringResource(Res.string.swish),
                text = if (method.isPending && phoneNumber.isBlank()) {
                  stringResource(Res.string.REFERRAL_PENDING_STATUS_LABEL)
                } else {
                  phoneNumber
                },
                isDefault = method.isDefault,
                onClick = onSwishSelected,
                modifier = Modifier.padding(horizontal = 16.dp)
              )
            }

            is PayinAccount.Trustly -> {
              val accountNumber = formatBankAccountNumber(method.clearingNumber, method.accountNumber)
              CurrentPayinMethodRow(
                label = formatBankAccountLabel(stringResource(Res.string.trustly), method.bankName),
                text = if (method.isPending && accountNumber.isBlank()) {
                  stringResource(Res.string.REFERRAL_PENDING_STATUS_LABEL)
                } else {
                  accountNumber
                },
                isDefault = method.isDefault,
                onClick = onTrustlySelected,
                modifier = Modifier.padding(horizontal = 16.dp)
              )
            }

            is PayinAccount.Invoice -> {
              CurrentPayinMethodRow(
                stringResource(Res.string.PAYMENTS_ACCOUNT),
                stringResource(Res.string.PAYMENTS_INVOICE),
                isDefault = method.isDefault,
                onClick = onInvoiceSelected,
                modifier = Modifier.padding(horizontal = 16.dp)
              )
            }
          }
        }
      }
    }
    Spacer(Modifier.weight(1f))
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      if (currentMethods.any { it.isPending }) {
        HedvigNotificationCard(
          message = "You have just added or changed a billing method, it will appear here soon.", //todo
          priority = NotificationPriority.Info,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
      }
      if (availablePayinMethods.isNotEmpty()) {
        HedvigButton(
          text = "Add a billing method", //todo!
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

@Composable
private fun CurrentPayinMethodRow(
  label: String,
  text: String,
  isDefault: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    onClick = onClick,
    modifier = modifier.fillMaxWidth(),
  ) {
    HorizontalItemsWithMaximumSpaceTaken(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      startSlot = {
        Column {
          HedvigText(text = label)
          HedvigText(
            text = text,
            color = HedvigTheme.colorScheme.textSecondary,
          )
        }
      },
      endSlot = {
        Row(
          horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        ){
          if (isDefault) {
            HighlightLabel(
              labelText = "Default", //todo
              size = HighlightLabelDefaults.HighLightSize.Small,
              color = HighlightLabelDefaults.HighlightColor.Green(
                HighlightLabelDefaults.HighlightShade.LIGHT
              )
            )
          }
        }
      },
      spaceBetween = 6.dp
    )
  }
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
        {},{},
{}      )
    }
  }
}

private class PayinAccountOverviewUiStateProvider : CollectionPreviewParameterProvider<PayinAccountOverviewUiState>(
  listOf(
    PayinAccountOverviewUiState.Loading,
    PayinAccountOverviewUiState.Error,
    PayinAccountOverviewUiState.Content(
      currentMethods = emptyList(),
      availablePayinMethods = listOf(MemberPaymentProvider.SWISH, MemberPaymentProvider.TRUSTLY),
    ),
    PayinAccountOverviewUiState.Content(
      currentMethods = listOf(
        PayinAccount.SwishPayin(
          phoneNumber = "070-123 45 67",
          isPending = false,
          isDefault = true,
        ),
      ),
      availablePayinMethods = listOf(MemberPaymentProvider.SWISH),
    ),
    PayinAccountOverviewUiState.Content(
      currentMethods = listOf(
        PayinAccount.SwishPayin(
          phoneNumber = "070-123 45 67",
          isPending = false,
          isDefault = true,
        ),
      ),
      availablePayinMethods = listOf(MemberPaymentProvider.SWISH, MemberPaymentProvider.TRUSTLY),
    ),
    PayinAccountOverviewUiState.Content(
      currentMethods = listOf(PayinAccount.SwishPayin(phoneNumber = null, isPending = true, isDefault = true)),
      availablePayinMethods = listOf(MemberPaymentProvider.SWISH),
    ),
    PayinAccountOverviewUiState.Content(
      currentMethods = listOf(
        PayinAccount.SwishPayin(
          phoneNumber = "070-123 45 67",
          isPending = true,
          isDefault = true,
        ),
      ),
      availablePayinMethods = listOf(MemberPaymentProvider.SWISH),
    ),
    PayinAccountOverviewUiState.Content(
      currentMethods = listOf(
        PayinAccount.Trustly(
          clearingNumber = "8327",
          accountNumber = "12345678",
          bankName = "Mock Swedbank",
          isPending = false,
          isDefault = true,
        ),
        PayinAccount.SwishPayin(
          phoneNumber = "070-123 45 67",
          isPending = true,
          isDefault = false,
        ),
      ),
      availablePayinMethods = listOf(MemberPaymentProvider.TRUSTLY),
    ),
  ),
)
