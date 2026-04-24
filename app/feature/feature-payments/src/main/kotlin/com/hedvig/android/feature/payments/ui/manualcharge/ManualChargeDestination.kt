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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.DropdownDefaults
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
import com.hedvig.android.design.system.hedvig.rememberHedvigBirthDateDateTimeFormatter
import com.hedvig.android.design.system.hedvig.rememberHedvigDateTimeFormatter
import com.hedvig.android.design.system.hedvig.rememberHedvigMonthDateTimeFormatter
import hedvig.resources.PAYMENTS_PAYMENT_OVERDUE_TITLE
import hedvig.resources.Res
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ManualChargeDestination(viewModel: ManualChargeViewModel, navigateUp: () -> Unit) {
  val uiState = viewModel.uiState.collectAsStateWithLifecycle()

  ManualChargeScreen(
    uiState = uiState.value,
    navigateUp = navigateUp,
    reload = { viewModel.emit(ManualChargeEvent.Retry) },
  )
}

@Composable
private fun ManualChargeScreen(
  uiState: ManualChargeUiState,
  navigateUp: () -> Unit,
  reload: () -> Unit,
) {
  HedvigScaffold(
    navigateUp = navigateUp,
    topAppBarText = stringResource(Res.string.PAYMENTS_PAYMENT_OVERDUE_TITLE),
  ) {
    when (uiState) {

      is ManualChargeUiState.Failure -> {
        //todo
        HedvigErrorSection(
          onButtonClick = reload,
          Modifier.weight(1f),
        )

      }

      ManualChargeUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgress(
          modifier = Modifier.weight(1f),
        )
      }

      is ManualChargeUiState.Success -> {
        ManualChargeSuccessScreen(uiState)
      }
    }
  }
}

@Composable
private fun ManualChargeSuccessScreen(uiState: ManualChargeUiState.Success) {
  val dateTimeFormatter = rememberHedvigMonthDateTimeFormatter()
  val dateTimeFormatterWithYear = rememberHedvigDateTimeFormatter()
  Column(
    modifier = Modifier
      .padding(16.dp)
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
          text = "Overdue since ${dateTimeFormatter.format (uiState.dueDate)}",
        )
        HedvigText(
          text = "Pay now to avoid interruption",
          color = HedvigTheme.colorScheme.textSecondary,
        )
      }
    }

    HedvigButton(
      text = "View payment details",
      onClick = { /* TODO: Navigate to payment details */ },
      enabled = true,
      modifier = Modifier.fillMaxWidth(),
      buttonStyle = ButtonDefaults.ButtonStyle.Ghost,
      buttonSize = ButtonDefaults.ButtonSize.Medium,
      border = HedvigTheme.colorScheme.borderPrimary
    )

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
          text = "Due date", //todo
          color = HedvigTheme.colorScheme.textSecondary,
          style = HedvigTheme.typography.label
        )
        HedvigText(
          text = dateTimeFormatterWithYear.format(uiState.dueDate),
          color = HedvigTheme.colorScheme.textSecondary,
          style = HedvigTheme.typography.label
        )
      }
      Spacer( Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        HedvigText(
          text = "Bank account", //todo
          color = HedvigTheme.colorScheme.textSecondary,
          style = HedvigTheme.typography.label
        )
        HedvigText(
          text = "*** *3242", //todo
          color = HedvigTheme.colorScheme.textSecondary,
          style = HedvigTheme.typography.label,
        )
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
        text = "Total",
      )
      HedvigText(
        text = uiState.amount.toString(),
        textAlign = TextAlign.End,
      )
    }

    HedvigButton(
      text = "Pay ${uiState.amount}",
      onClick = { /* TODO: Handle payment */ },
      enabled = true,
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(8.dp))
    HedvigText(
      text = "Ensure your account has enough\nfunds to cover this payment",
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
          dueDate = LocalDate(2026, 1, 1),
          amount = UiMoney(100.0, UiCurrencyCode.SEK),
        ),
        navigateUp = {},
        reload = {},
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
        uiState = ManualChargeUiState.Failure(ManualChargeFailureReason.GeneralFailure),
        navigateUp = {},
        reload = {},
      )
    }
  }
}


