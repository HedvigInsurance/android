package com.hedvig.android.feature.payments.ui.manualcharge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.compose.ui.preview.BooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.data.paying.member.PayinAccount.Invoice
import com.hedvig.android.data.paying.member.PayinAccount.SwishPayin
import com.hedvig.android.data.paying.member.PayinAccount.Trustly
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.data.paying.member.maskedAccountNumber
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonSize
import com.hedvig.android.design.system.hedvig.ButtonDefaults.ButtonStyle
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.NotificationDefaults.NotificationPriority.AttentionRound
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.WarningFilled
import com.hedvig.android.design.system.hedvig.rememberHedvigDateTimeFormatter
import com.hedvig.android.design.system.hedvig.rememberHedvigMonthDateTimeFormatter
import com.hedvig.android.feature.payments.data.ManualChargeInfo
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeEvent.Refresh
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeEvent.Retry
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeEvent.TriggerCharge
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeUiState.Failure
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeUiState.Loading
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeUiState.NoLongerChargeable
import com.hedvig.android.feature.payments.ui.manualcharge.ManualChargeUiState.Success
import hedvig.resources.GENERAL_ERROR_BODY
import hedvig.resources.GENERAL_RETRY
import hedvig.resources.MANUAL_CHARGE_CANCELLATION_WARNING
import hedvig.resources.PAYMENTS_ACCOUNT
import hedvig.resources.PAYMENTS_AUTOGIRO_LABEL
import hedvig.resources.PAYMENTS_BANK_LABEL
import hedvig.resources.PAYMENTS_INVOICE
import hedvig.resources.PAYMENTS_PAYMENT_METHOD
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_DETAILS_BODY
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_DETAILS_DUE_DATE
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_DETAILS_FINE_PRINT
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_DETAILS_PAY
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_DETAILS_SINCE
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_DETAILS_VIEW_DETAILS
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_TITLE
import hedvig.resources.PAYMENTS_SWISH_NUMBER
import hedvig.resources.PAYMENT_ADD_METHOD_BUTTON
import hedvig.resources.PAYMENT_CHOOSE_PRIMARY_BUTTON
import hedvig.resources.Res
import hedvig.resources.SELF_MANUAL_CHARGE_CHANGES_BEEN_MADE_TITLE
import hedvig.resources.claim_status_detail_chat_button_description
import hedvig.resources.payment_details_receipt_card_total
import hedvig.resources.something_went_wrong
import hedvig.resources.swish
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ManualChargeDestination(
  viewModel: ManualChargeViewModel,
  navigateUp: () -> Unit,
  onNavigateToPaymentDetails: (chargeId: String) -> Unit,
  onConnectPayinMethodClicked: () -> Unit,
  onChoosePrimaryMethodClicked: () -> Unit,
  openConversation: () -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle()
  var hasResumedOnce by rememberSaveable { mutableStateOf(false) }
  LifecycleResumeEffect(Unit) {
    if (hasResumedOnce) {
      viewModel.emit(Refresh)
    } else {
      hasResumedOnce = true
    }
    onPauseOrDispose {}
  }

  ManualChargeScreen(
    uiState = uiState.value,
    navigateUp = navigateUp,
    reload = { viewModel.emit(Retry) },
    onNavigateToPaymentDetails = onNavigateToPaymentDetails,
    onTriggerPayment = {
      viewModel.emit(TriggerCharge)
    },
    onConnectPayinMethodClicked = onConnectPayinMethodClicked,
    onChoosePrimaryMethodClicked = onChoosePrimaryMethodClicked,
    openConversation = openConversation,
  )
}

@Composable
private fun ManualChargeScreen(
  uiState: ManualChargeUiState,
  navigateUp: () -> Unit,
  reload: () -> Unit,
  openConversation: () -> Unit,
  onNavigateToPaymentDetails: (chargeId: String) -> Unit,
  onTriggerPayment: () -> Unit,
  onConnectPayinMethodClicked: () -> Unit,
  onChoosePrimaryMethodClicked: () -> Unit,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(Res.string.PAYMENTS_PAYMENT_OVERDUE_TITLE),
  ) {
    when (uiState) {
      is Failure -> {
        val title = if (uiState.error.message != null) {
          stringResource(Res.string.SELF_MANUAL_CHARGE_CHANGES_BEEN_MADE_TITLE)
        } else {
          stringResource(Res.string.something_went_wrong)
        }
        val subTitle = if (uiState.error.message != null) {
          uiState.error.message
        } else {
          stringResource(Res.string.GENERAL_ERROR_BODY)
        }
        val buttonText = if (uiState.error.message != null) {
          stringResource(Res.string.claim_status_detail_chat_button_description)
        } else {
          stringResource(Res.string.GENERAL_RETRY)
        }
        val onButtonClick = if (uiState.error.message != null) openConversation else reload

        HedvigErrorSection(
          onButtonClick = onButtonClick,
          Modifier
            .weight(1f)
            .fillMaxWidth(),
          subTitle = subTitle,
          buttonText = buttonText,
          title = title,
        )
      }

      NoLongerChargeable -> {
        HedvigErrorSection(
          onButtonClick = openConversation,
          Modifier
            .weight(1f)
            .fillMaxWidth(),
          title = stringResource(Res.string.SELF_MANUAL_CHARGE_CHANGES_BEEN_MADE_TITLE),
          subTitle = null,
          buttonText = stringResource(Res.string.claim_status_detail_chat_button_description),
        )
      }

      Loading -> {
        HedvigFullScreenCenterAlignedProgress(
          modifier = Modifier.weight(1f),
        )
      }

      is Success -> {
        ManualChargeSuccessScreen(
          uiState,
          onNavigateToPaymentDetails = onNavigateToPaymentDetails,
          onTriggerPayment = onTriggerPayment,
          onConnectPayinMethodClicked = onConnectPayinMethodClicked,
          onChoosePrimaryMethodClicked = onChoosePrimaryMethodClicked,
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun ManualChargeSuccessScreen(
  uiState: Success,
  onNavigateToPaymentDetails: (chargeId: String) -> Unit,
  onTriggerPayment: () -> Unit,
  onConnectPayinMethodClicked: () -> Unit,
  onChoosePrimaryMethodClicked: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val currentMethods = uiState.manualChargeInfo.currentMethods
  val availablePayinMethods = uiState.manualChargeInfo.availablePayinMethods
  val dateTimeFormatter = rememberHedvigMonthDateTimeFormatter()
  val dateTimeFormatterWithYear = rememberHedvigDateTimeFormatter()
  Column(
    modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState()),
  ) {
    Column(
      modifier = Modifier
        .padding(
          top = 8.dp,
          start = 16.dp,
          end = 16.dp,
          bottom = 16.dp,
        )
        .hedvigDropShadow(HedvigTheme.shapes.cornerXLarge)
        .fillMaxWidth()
        .background(
          color = HedvigTheme.colorScheme.backgroundPrimary,
          shape = HedvigTheme.shapes.cornerXLarge,
        )
        .border(
          width = 1.dp,
          color = HedvigTheme.colorScheme.borderPrimary,
          shape = HedvigTheme.shapes.cornerXLarge,
        )
        .clip(HedvigTheme.shapes.cornerXLarge)
        .padding(16.dp),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        verticalAlignment = Alignment.Top,
      ) {
        Icon(
          imageVector = HedvigIcons.WarningFilled,
          contentDescription = null,
          tint = HedvigTheme.colorScheme.signalRedElement,
          modifier = Modifier.size(40.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          HedvigText(
            text = stringResource(
              Res.string.PAYMENTS_PAYMENT_OVERDUE_DETAILS_SINCE,
              dateTimeFormatter.format(uiState.manualChargeInfo.missedDueDate),
            ),
          )
          HedvigText(
            text = stringResource(Res.string.PAYMENTS_PAYMENT_OVERDUE_DETAILS_BODY),
            color = HedvigTheme.colorScheme.textSecondary,
          )
        }
      }
      if (uiState.manualChargeInfo.chargeId != null) {
        HedvigButton(
          text = stringResource(Res.string.PAYMENTS_PAYMENT_OVERDUE_DETAILS_VIEW_DETAILS),
          onClick = {
            onNavigateToPaymentDetails(uiState.manualChargeInfo.chargeId)
          },
          enabled = true,
          modifier = Modifier.fillMaxWidth(),
          buttonStyle = ButtonStyle.Ghost,
          buttonSize = ButtonSize.Medium,
          border = HedvigTheme.colorScheme.borderPrimary,
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
      Column(
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          HedvigText(
            text = stringResource(Res.string.PAYMENTS_PAYMENT_OVERDUE_DETAILS_DUE_DATE),
            color = HedvigTheme.colorScheme.textSecondary,
            style = HedvigTheme.typography.label,
          )
          HedvigText(
            text = dateTimeFormatterWithYear.format(uiState.manualChargeInfo.missedDueDate),
            color = HedvigTheme.colorScheme.textSecondary,
            style = HedvigTheme.typography.label,
          )
        }
        val method = uiState.manualChargeInfo.primaryPayinMethod
        if (method != null) {
          DetailRow(
            label = stringResource(Res.string.PAYMENTS_PAYMENT_METHOD),
            value = when (method) {
              is Trustly -> stringResource(Res.string.PAYMENTS_AUTOGIRO_LABEL)
              is SwishPayin -> stringResource(Res.string.swish)
              is Invoice -> stringResource(Res.string.PAYMENTS_INVOICE)
            },
          )
          when (method) {
            is SwishPayin -> {
              DetailRow(
                label = stringResource(Res.string.PAYMENTS_SWISH_NUMBER),
                value = method.phoneNumber.orEmpty(),
              )
            }

            is Trustly -> {
              DetailRow(
                label = stringResource(Res.string.PAYMENTS_ACCOUNT),
                value = method.maskedAccountNumber().orEmpty(),
              )
              DetailRow(
                label = stringResource(Res.string.PAYMENTS_BANK_LABEL),
                value = method.bankName.orEmpty(),
              )
            }

            is Invoice -> {}
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
      HorizontalDivider()
      Spacer(modifier = Modifier.height(16.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        HedvigText(
          text = stringResource(Res.string.payment_details_receipt_card_total),
        )
        HedvigText(
          text = uiState.manualChargeInfo.amountDue.toString(),
          textAlign = TextAlign.End,
        )
      }

      HedvigButton(
        text = stringResource(Res.string.PAYMENTS_PAYMENT_OVERDUE_DETAILS_PAY, uiState.manualChargeInfo.amountDue),
        onClick = onTriggerPayment,
        enabled = !uiState.payButtonLoading,
        buttonSize = ButtonSize.Medium,
        modifier = Modifier.fillMaxWidth(),
        isLoading = uiState.payButtonLoading,
      )

      Spacer(modifier = Modifier.height(8.dp))
      HedvigText(
        text = stringResource(Res.string.PAYMENTS_PAYMENT_OVERDUE_DETAILS_FINE_PRINT),
        color = HedvigTheme.colorScheme.textSecondaryTranslucent,
        textAlign = TextAlign.Center,
        style = HedvigTheme.typography.label,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
    }
    if (uiState.manualChargeInfo.showCancellationWarning) {
      HedvigNotificationCard(
        message = stringResource(Res.string.MANUAL_CHARGE_CANCELLATION_WARNING),
        priority = AttentionRound,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
      Spacer(Modifier.height(16.dp))
    }
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    if (availablePayinMethods.isNotEmpty()) {
      HedvigButton(
        text = stringResource(Res.string.PAYMENT_ADD_METHOD_BUTTON),
        onClick = onConnectPayinMethodClicked,
        enabled = true,
        buttonStyle = ButtonStyle.Secondary,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
      )
    }
    if (currentMethods
        .filter { !it.isPending }
        .size > 1
    ) {
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
private fun DetailRow(label: String, value: String, modifier: Modifier = Modifier) {
  Column(modifier) {
    Spacer(Modifier.height(10.dp))
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      HedvigText(
        text = label,
        color = HedvigTheme.colorScheme.textSecondary,
        style = HedvigTheme.typography.label,
      )
      HedvigText(
        text = value,
        color = HedvigTheme.colorScheme.textSecondary,
        style = HedvigTheme.typography.label,
      )
    }
  }
}

@Composable
@Preview
@HedvigShortMultiScreenPreview
private fun ManualChargeScreenSuccessPreview(
  @PreviewParameter(
    BooleanCollectionPreviewParameterProvider::class,
  ) showCancellationWarning: Boolean,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ManualChargeScreen(
        uiState = Success(
          ManualChargeInfo(
            missedDueDate = LocalDate(2026, 1, 15),
            amountDue = UiMoney(100.0, UiCurrencyCode.SEK),
            chargeId = "chargeId",
            currentMethods = listOf(
              SwishPayin(
                "123456",
                isPending = false,
                isDefault = true,
              ),
              Trustly(
                clearingNumber = "8327",
                accountNumber = "91234124",
                bankName = "Swedbank",
                isPending = false,
                isDefault = true,
              ),
            ),
            availablePayinMethods = listOf(
              PaymentProvider.Swish,
              PaymentProvider.Trustly,
            ),
            primaryPayinMethod = Trustly(
              clearingNumber = "8327",
              accountNumber = "91234124",
              bankName = "Swedbank",
              isPending = false,
              isDefault = true,
            ),
            showCancellationWarning = showCancellationWarning,
          ),
        ),
        navigateUp = {},
        reload = {},
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

@Composable
@Preview
@HedvigPreview
private fun ManualChargeScreenLoadingPreview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ManualChargeScreen(
        uiState = Loading,
        navigateUp = {},
        reload = {},
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}

@Composable
@Preview
@HedvigPreview
private fun ManualChargeScreenFailurePreview(
  @PreviewParameter(
    BooleanCollectionPreviewParameterProvider::class,
  ) hasUserError: Boolean,
) {
  HedvigTheme {
    Surface {
      ManualChargeScreen(
        uiState = Failure(
          ErrorMessage(
            message = if (hasUserError) {
              "Cannot charge the failed payment since there have been some changes. " +
                "The new amount will be included in the upcoming payment."
            } else {
              null
            },
          ),
        ),
        navigateUp = {},
        reload = {},
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}
