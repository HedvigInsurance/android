package com.hedvig.android.feature.payin.account.ui.overview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.EmptyState
import com.hedvig.android.design.system.hedvig.EmptyStateDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigButtonGhostWithBorder
import com.hedvig.android.design.system.hedvig.HedvigCard
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HighlightLabel
import com.hedvig.android.design.system.hedvig.HighlightLabelDefaults
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.icon.Autogiro
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.colored.Kivra
import com.hedvig.android.design.system.hedvig.icon.colored.Swish
import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.feature.payin.account.data.toDeliveryString
import com.hedvig.android.logger.logcat
import hedvig.resources.PAYMENTS_INVOICE
import hedvig.resources.REFERRAL_PENDING_STATUS_LABEL
import hedvig.resources.Res
import hedvig.resources.something_went_wrong
import hedvig.resources.swish
import octopus.type.MemberPaymentProvider
import octopus.type.PaymentMethodInvoiceDelivery
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
    setAsDefaultPayinMethod = {
      viewModel.emit(PayinAccountOverviewEvent.SetDefaultMethod(it))
    },
  )
}

@Composable
private fun PayinAccountOverviewScreen(
  uiState: PayinAccountOverviewUiState,
  onConnectPayoutMethodClicked: () -> Unit,
  onRetry: () -> Unit,
  navigateUp: () -> Unit,
  setAsDefaultPayinMethod: (MemberPaymentProvider) -> Unit,
) {
  HedvigScaffold(
    topAppBarText = "Billing account", // todo!
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
          setAsDefaultPayinMethod = setAsDefaultPayinMethod,
          loadingDefaultProvider = uiState.loadingDefaultProvider,
          setDefaultProviderError = uiState.setDefaultProviderError,
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
  setAsDefaultPayinMethod: (MemberPaymentProvider) -> Unit,
  loadingDefaultProvider: MemberPaymentProvider?,
  setDefaultProviderError: ErrorMessage?,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Spacer(Modifier.height(8.dp))
    if (currentMethods.isEmpty()) {
      if (availablePayinMethods.isNotEmpty()) {
        Spacer(Modifier.weight(1f))
        EmptyState(
          text = "You haven’t added a billing method yet. Add one to pay for your insurance.", // todo
          description = null,
          iconStyle = EmptyStateDefaults.EmptyStateIconStyle.INFO,
        )
      }
    } else {
      HedvigText(
        "Active billing methods", // todo
        modifier = Modifier.padding(horizontal = 16.dp),
      )
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
                  formatSwishPhoneNumber(phoneNumber)
                },
                isDefault = method.isDefault,
                onClick = setAsDefaultPayinMethod,
                modifier = Modifier.padding(horizontal = 16.dp),
                loadingDefaultProvider = loadingDefaultProvider,
                isPending = method.isPending,
                provider = MemberPaymentProvider.SWISH,
              )
            }

            is PayinAccount.Trustly -> {
              val accountNumber = formatBankAccountNumber(method.clearingNumber, method.accountNumber, method.bankName)
              CurrentPayinMethodRow(
                label = "Direct debit", // todo
                text = if (method.isPending && accountNumber.isBlank()) {
                  stringResource(Res.string.REFERRAL_PENDING_STATUS_LABEL)
                } else {
                  accountNumber
                },
                isDefault = method.isDefault,
                onClick = setAsDefaultPayinMethod,
                modifier = Modifier.padding(horizontal = 16.dp),
                loadingDefaultProvider = loadingDefaultProvider,
                isPending = method.isPending,
                provider = MemberPaymentProvider.TRUSTLY,
              )
            }

            is PayinAccount.Invoice -> {
              CurrentPayinMethodRow(
                stringResource(Res.string.PAYMENTS_INVOICE),
                method.delivery?.toDeliveryString() ?: "",
                isDefault = method.isDefault,
                onClick = setAsDefaultPayinMethod,
                modifier = Modifier.padding(horizontal = 16.dp),
                loadingDefaultProvider = loadingDefaultProvider,
                isPending = method.isPending,
                provider = MemberPaymentProvider.INVOICE,
              )
            }
          }
        }
      }
    }
    Column(
      Modifier
        .weight(1f)
        .fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      if (setDefaultProviderError != null) {
        EmptyState(
          text = stringResource(Res.string.something_went_wrong),
          description = setDefaultProviderError.message,
          modifier = Modifier.padding(horizontal = 16.dp),
          iconStyle = EmptyStateDefaults.EmptyStateIconStyle.ERROR,
        )
      }
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      if (currentMethods.any { it.isPending }) {
        HedvigNotificationCard(
          message = "You have just added or changed a billing method, it will appear here soon.", // todo
          priority = NotificationPriority.Info,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
      }
      if (availablePayinMethods.isNotEmpty()) {
        HedvigButton(
          text = "Add a billing method", // todo!
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
private fun CurrentPayinMethodRow(
  label: String,
  text: String,
  isDefault: Boolean,
  isPending: Boolean,
  loadingDefaultProvider: MemberPaymentProvider?,
  onClick: (MemberPaymentProvider) -> Unit,
  provider: MemberPaymentProvider,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    modifier = modifier.fillMaxWidth(),
  ) {
    HorizontalItemsWithMaximumSpaceTaken(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      startSlot = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
        ) {
          when (provider) {
            MemberPaymentProvider.TRUSTLY -> Icon(
              HedvigIcons.Autogiro,
              null, //todo
              modifier = Modifier.size(32.dp),
            )

            MemberPaymentProvider.SWISH -> Image(
              HedvigIcons.Swish,
              null,  //todo
              modifier = Modifier.size(32.dp),
            )

            MemberPaymentProvider.INVOICE ->  Image(
              HedvigIcons.Kivra,
              null,  //todo
              modifier = Modifier.size(32.dp),
            )
            else -> {}
          }

          Spacer(Modifier.width(16.dp))
          Column {
            HedvigText(text = label)
            HedvigText(
              text = text ,
              color = HedvigTheme.colorScheme.textSecondary,
            )
          }
        }
      },
      endSlot = {
        Row(
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          if (isDefault) {
            HighlightLabel(
              labelText = "Default", // todo
              size = HighlightLabelDefaults.HighLightSize.Small,
              color = HighlightLabelDefaults.HighlightColor.Green(
                HighlightLabelDefaults.HighlightShade.LIGHT,
              ),
            )
          } else {
            if (!isPending) {
              HedvigButtonGhostWithBorder(
                size = ButtonDefaults.ButtonSize.Small,
                text = "Set as default", // todo
                onClick = {
                  onClick(provider)
                },
                isLoading = loadingDefaultProvider == provider,
              )
            }
          }
        }
      },
      spaceBetween = 6.dp,
    )
  }
}

internal fun formatSwishPhoneNumber(phoneNumber: String): String {
  val digits = phoneNumber.take(15)
  val sb = StringBuilder()
  for (i in digits.indices) {
    sb.append(digits[i])
    if (i in setOf(2, 5, 7)
      ) {
      sb.append("-")
    }
  }
  return sb.toString()
}


private fun formatBankAccountNumber(clearingNumber: String?, accountNumber: String?, bankName: String?): String {
  logcat { "Mariia: clearingNumber: $clearingNumber accountNumber: $accountNumber bankName: $bankName" }
//  return when {
//    clearingNumber != null && accountNumber != null && bankName != null -> "$bankName $clearingNumber-$accountNumber"
//    clearingNumber != null && bankName != null -> "$bankName $clearingNumber"
//    else -> clearingNumber.orEmpty()
//  }
  return when {
    bankName != null -> bankName
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
        {},
      )
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
        PayinAccount.Trustly(
          clearingNumber = "****",
          accountNumber = "*45678",
          bankName = "Swedbank",
          isPending = false,
          isDefault = true,
        ),
        PayinAccount.SwishPayin(
          phoneNumber = "070-123 45 67",
          isPending = false,
          isDefault = false,
        ),
      ),
      availablePayinMethods = listOf(MemberPaymentProvider.SWISH),
      loadingDefaultProvider = MemberPaymentProvider.SWISH,
    ),
    PayinAccountOverviewUiState.Content(
      currentMethods = listOf(
        PayinAccount.Trustly(
          clearingNumber = "****",
          accountNumber = "*45678",
          bankName = "Swedbank",
          isPending = false,
          isDefault = true,
        ),
        PayinAccount.SwishPayin(
          phoneNumber = "070-123 45 67",
          isPending = false,
          isDefault = false,
        ),
        PayinAccount.Invoice (
          delivery =  PaymentMethodInvoiceDelivery.KIVRA,
          isPending = false,
          isDefault = false,
          email = ""
        ),
      ),
      availablePayinMethods = listOf(MemberPaymentProvider.TRUSTLY),
    ),
    PayinAccountOverviewUiState.Content(
      currentMethods = listOf(
        PayinAccount.Trustly(
          clearingNumber = "****",
          accountNumber = "*45678",
          bankName = "Swedbank",
          isPending = false,
          isDefault = true,
        ),
        PayinAccount.SwishPayin(
          phoneNumber = "070-123 45 67",
          isPending = false,
          isDefault = false,
        ),
      ),
      availablePayinMethods = listOf(MemberPaymentProvider.TRUSTLY),
      setDefaultProviderError = ErrorMessage("Not possible to set this method as default"),
    ),
  ),
)
