package com.hedvig.android.feature.payments2

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.animation.animateContentHeight
import com.hedvig.android.core.designsystem.component.button.HedvigContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.material3.typeElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.ChevronRight
import com.hedvig.android.core.icons.hedvig.normal.Payments
import com.hedvig.android.core.icons.hedvig.normal.Waiting
import com.hedvig.android.core.icons.hedvig.small.hedvig.Campaign
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.preview.DoubleBooleanCollectionPreviewParameterProvider
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.core.uidata.UiMoney
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import octopus.type.CurrencyCode

@Composable
internal fun PaymentOverviewDestination(
  viewModel: PaymentOverviewViewModel,
  onBackPressed: () -> Unit,
  onUpcomingPaymentClicked: () -> Unit,
  onDiscountClicked: () -> Unit,
  onPaymentHistoryClicked: () -> Unit,
  onChangeBankAccount: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  PaymentOverviewScreen(
    uiState = uiState,
    navigateUp = onBackPressed,
    onUpcomingPaymentClicked = onUpcomingPaymentClicked,
    onChangeBankAccount = onChangeBankAccount,
    onDiscountClicked = onDiscountClicked,
    onPaymentHistoryClicked = onPaymentHistoryClicked,
    onRetry = { viewModel.emit(PaymentEvent.Retry) },
  )
}

@Composable
private fun PaymentOverviewScreen(
  uiState: OverViewUiState,
  navigateUp: () -> Unit,
  onUpcomingPaymentClicked: () -> Unit,
  onChangeBankAccount: () -> Unit,
  onDiscountClicked: () -> Unit,
  onPaymentHistoryClicked: () -> Unit,
  onRetry: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = stringResource(R.string.PROFILE_PAYMENT_TITLE),
    navigateUp = navigateUp,
    modifier = Modifier.clearFocusOnTap(),
  ) {
    when (uiState) {
      is OverViewUiState.Content -> Column {
        PaymentAmountCard(
          uiState = uiState,
          onUpcomingPaymentClicked = onUpcomingPaymentClicked,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .animateContentHeight(),
        )
        Discounts(
          modifier = Modifier
            .clickable { onDiscountClicked() }
            .padding(16.dp),
        )
        Divider(modifier = Modifier.padding(horizontal = 16.dp))
        PaymentHistory(
          modifier = Modifier
            .clickable { onPaymentHistoryClicked() }
            .padding(16.dp),
        )
        Divider(modifier = Modifier.padding(horizontal = 16.dp))
        PaymentDetails(
          displayName = uiState.connectedPaymentDisplayName,
          displayValue = uiState.connectedPaymentValue,
        )
        Spacer(Modifier.height(16.dp))
        if (uiState.hasConnectedPayment) {
          HedvigContainedButton(
            text = stringResource(R.string.PROFILE_PAYMENT_CHANGE_BANK_ACCOUNT),
            onClick = onChangeBankAccount,
            modifier = Modifier.padding(horizontal = 16.dp),
          )
        } else {
          HedvigContainedButton(
            text = stringResource(R.string.PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_BUTTON),
            onClick = onChangeBankAccount,
            modifier = Modifier.padding(horizontal = 16.dp),
          )
        }
        Spacer(Modifier.height(16.dp))
      }

      is OverViewUiState.Error -> Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
        HedvigErrorSection(retry = onRetry)
      }

      OverViewUiState.Loading -> HedvigFullScreenCenterAlignedProgressDebounced()
    }
  }
}

@Composable
private fun PaymentAmountCard(
  uiState: OverViewUiState.Content,
  onUpcomingPaymentClicked: () -> Unit,
  modifier: Modifier = Modifier,
) {
  HedvigCard(
    onClick = onUpcomingPaymentClicked,
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .fillMaxWidth(),
    ) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = { Text(stringResource(id = R.string.PAYMENTS_UPCOMING_PAYMENT)) },
        endSlot = {
          Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = uiState.upcomingPayment.toString(),
              textAlign = TextAlign.End,
            )
            Spacer(Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Hedvig.ChevronRight,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp),
            )
          }
        },
      )
      val dateTimeFormatter = rememberHedvigDateTimeFormatter()
      Text(
        text = stringResource(
          id = R.string.GENERAL_DUE,
          dateTimeFormatter.format(uiState.dueDate.toJavaLocalDate()),
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun PaymentDetails(displayName: String, displayValue: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.weight(1f),
    ) {
      Icon(
        imageVector = Icons.Hedvig.Payments,
        contentDescription = null,
        modifier = Modifier.size(24.dp),
      )
      Spacer(Modifier.width(16.dp))
      Text(displayName)
    }
    Spacer(Modifier.width(8.dp))
    Text(
      text = displayValue,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun Discounts(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = Icons.Hedvig.Campaign,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.typeElement,
      modifier = Modifier.size(24.dp),
    )
    Spacer(Modifier.width(16.dp))
    Text(stringResource(id = R.string.PAYMENTS_DISCOUNTS_SECTION_TITLE))
  }
}

@Composable
private fun PaymentHistory(modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = Icons.Hedvig.Waiting,
      contentDescription = null,
      modifier = Modifier.size(24.dp),
    )
    Spacer(Modifier.width(16.dp))
    Text(stringResource(id = R.string.PAYMENTS_PAYMENT_HISTORY_BUTTON_LABEL))
  }
}

@Composable
@HedvigPreview
private fun PreviewPaymentScreen(
  @PreviewParameter(DoubleBooleanCollectionPreviewParameterProvider::class) input: Pair<Boolean, Boolean>,
) {
  val hasPaymentConnected = input.first
  val hasCampaigns = input.second
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      PaymentOverviewScreen(
        uiState = OverViewUiState.Content(
          upcomingPayment = UiMoney(1534.0, CurrencyCode.SEK),
          dueDate = LocalDate.fromEpochDays(400),
          connectedPaymentDisplayName = "Nordea",
          connectedPaymentValue = "31489*****",
          hasConnectedPayment = hasPaymentConnected,
        ),
        {},
        {},
        {},
        {},
        {},
        {},
      )
    }
  }
}
