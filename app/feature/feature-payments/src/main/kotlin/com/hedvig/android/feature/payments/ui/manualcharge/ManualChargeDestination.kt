package com.hedvig.android.feature.payments.ui.manualcharge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.WarningFilled
import com.hedvig.android.design.system.hedvig.rememberHedvigDateTimeFormatter
import com.hedvig.android.design.system.hedvig.rememberHedvigMonthDateTimeFormatter
import com.hedvig.android.feature.payments.data.ManualChargeInfo
import hedvig.resources.GENERAL_ERROR_BODY
import hedvig.resources.GENERAL_RETRY
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_DETAILS_BODY
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_DETAILS_DUE_DATE
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_DETAILS_FINE_PRINT
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_DETAILS_PAY
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_DETAILS_SINCE
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_DETAILS_VIEW_DETAILS
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_TITLE
import hedvig.resources.Res
import hedvig.resources.general_close_button
import hedvig.resources.payment_details_receipt_card_total
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ManualChargeDestination(
  viewModel: ManualChargeViewModel,
  navigateUp: () -> Unit,
  onNavigateToPaymentDetails: (chargeId: String) -> Unit,
  onNavigateToSuccess: () -> Unit,
) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle()

  ManualChargeScreen(
    uiState = uiState.value,
    navigateUp = navigateUp,
    reload = { viewModel.emit(ManualChargeEvent.Retry) },
    onNavigateToPaymentDetails = onNavigateToPaymentDetails,
    onNavigateToSuccess = {
      viewModel.emit(ManualChargeEvent.ClearNav)
      onNavigateToSuccess()
    },
    onTriggerPayment = {
      viewModel.emit(ManualChargeEvent.TriggerCharge) }
  )
}

@Composable
private fun ManualChargeScreen(
  uiState: ManualChargeUiState,
  navigateUp: () -> Unit,
  reload: () -> Unit,
  onNavigateToPaymentDetails: (chargeId: String) -> Unit,
  onNavigateToSuccess: () -> Unit,
  onTriggerPayment: () -> Unit
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(Res.string.PAYMENTS_PAYMENT_OVERDUE_TITLE),
  ) {
    when (uiState) {

      is ManualChargeUiState.Failure -> {
        val subTitle = if (uiState.error.message!=null) uiState.error.message else
          stringResource(Res.string.GENERAL_ERROR_BODY)
        val buttonText = if (uiState.error.message!=null) stringResource(Res.string.general_close_button) else
          stringResource(Res.string.GENERAL_RETRY)
        val onButtonClick = if (uiState.error.message!=null) navigateUp else reload

        HedvigErrorSection(
          onButtonClick = onButtonClick,
          Modifier.weight(1f).fillMaxWidth(),
          subTitle = subTitle,
          buttonText = buttonText
        )

      }

      ManualChargeUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgress(
          modifier = Modifier.weight(1f),
        )
      }

      is ManualChargeUiState.Success -> {
        if (uiState.navigateToSuccess!=null) {
          LaunchedEffect(uiState.navigateToSuccess) {
            onNavigateToSuccess()
          }
        } else {
          ManualChargeSuccessScreen(
            uiState,
            onNavigateToPaymentDetails = onNavigateToPaymentDetails,
            onTriggerPayment = onTriggerPayment
          )
        }
      }
    }
  }
}

@Composable
private fun ManualChargeSuccessScreen(
  uiState: ManualChargeUiState.Success,
  onNavigateToPaymentDetails: (chargeId: String) -> Unit,
  onTriggerPayment: () -> Unit,
) {
  val dateTimeFormatter = rememberHedvigMonthDateTimeFormatter()
  val dateTimeFormatterWithYear = rememberHedvigDateTimeFormatter()
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
    if (uiState.manualChargeInfo.chargeId!=null) {
      HedvigButton(
        text = stringResource(Res.string.PAYMENTS_PAYMENT_OVERDUE_DETAILS_VIEW_DETAILS),
        onClick = {
          onNavigateToPaymentDetails(uiState.manualChargeInfo.chargeId)
        },
        enabled = true,
        modifier = Modifier.fillMaxWidth(),
        buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
        buttonSize = ButtonDefaults.ButtonSize.Medium,
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
      if (uiState.manualChargeInfo.bankDescriptor!=null &&
        uiState.manualChargeInfo.bankAccountDisplayValue!=null) {
        Spacer(Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          HedvigText(
            text = uiState.manualChargeInfo.bankDescriptor,
            color = HedvigTheme.colorScheme.textSecondary,
            style = HedvigTheme.typography.label,
          )
          HedvigText(
            text = uiState.manualChargeInfo.bankAccountDisplayValue,
            color = HedvigTheme.colorScheme.textSecondary,
            style = HedvigTheme.typography.label,
          )
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
      enabled = true,
      modifier = Modifier.fillMaxWidth(),
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

}

@Composable
@Preview
@HedvigPreview
private fun ManualChargeScreenSuccessPreview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      ManualChargeScreen(
        uiState = ManualChargeUiState.Success(
          ManualChargeInfo(
            missedDueDate = LocalDate(2026, 1, 15),
            amountDue = UiMoney(100.0, UiCurrencyCode.SEK),
            chargeId = "chargeId",
            bankDescriptor = "Bank account",
            bankAccountDisplayValue = "**** 8324"
          ),
          navigateToSuccess = null
        ),
        navigateUp = {},
        reload = {},
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
    Surface {
      ManualChargeScreen(
        uiState = ManualChargeUiState.Loading,
        navigateUp = {},
        reload = {},
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
private fun ManualChargeScreenFailurePreview() {
  HedvigTheme {
    Surface {
      ManualChargeScreen(
        uiState = ManualChargeUiState.Failure(ErrorMessage("Payment method not allowed")),
        navigateUp = {},
        reload = {},
        {},
        {},
        {},
      )
    }
  }
}


