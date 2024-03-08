package com.hedvig.android.feature.payments.overview

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.animation.animateContentHeight
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.button.HedvigSecondaryContainedButton
import com.hedvig.android.core.designsystem.component.card.HedvigCard
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.information.HedvigInformationSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.material3.containedButtonContainer
import com.hedvig.android.core.designsystem.material3.onContainedButtonContainer
import com.hedvig.android.core.designsystem.material3.typeElement
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.ChevronRight
import com.hedvig.android.core.icons.hedvig.normal.Payments
import com.hedvig.android.core.icons.hedvig.normal.Waiting
import com.hedvig.android.core.icons.hedvig.small.hedvig.Campaign
import com.hedvig.android.core.ui.clearFocusOnTap
import com.hedvig.android.core.ui.infocard.VectorErrorCard
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.infocard.VectorWarningCard
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.feature.payments.data.Discount
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.PaymentConnection
import com.hedvig.android.feature.payments.data.PaymentOverview
import com.hedvig.android.feature.payments.discountsPreviewData
import com.hedvig.android.feature.payments.paymentOverViewPreviewData
import com.hedvig.android.pullrefresh.PullRefreshDefaults
import com.hedvig.android.pullrefresh.PullRefreshIndicator
import com.hedvig.android.pullrefresh.pullRefresh
import com.hedvig.android.pullrefresh.rememberPullRefreshState
import hedvig.resources.R
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun PaymentOverviewDestination(
  viewModel: PaymentOverviewViewModel,
  onBackPressed: () -> Unit,
  onUpcomingPaymentClicked: (MemberCharge, PaymentOverview) -> Unit,
  onDiscountClicked: (List<Discount>) -> Unit,
  onPaymentHistoryClicked: (PaymentOverview) -> Unit,
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
  onUpcomingPaymentClicked: (MemberCharge, PaymentOverview) -> Unit,
  onChangeBankAccount: () -> Unit,
  onDiscountClicked: (List<Discount>) -> Unit,
  onPaymentHistoryClicked: (PaymentOverview) -> Unit,
  onRetry: () -> Unit,
) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()
  val systemBarInsetTopDp = with(LocalDensity.current) {
    WindowInsets.systemBars.getTop(this).toDp()
  }
  val pullRefreshState = rememberPullRefreshState(
    refreshing = uiState.isRetrying,
    onRefresh = onRetry,
    refreshingOffset = PullRefreshDefaults.RefreshingOffset + systemBarInsetTopDp,
  )
  Box(Modifier.fillMaxSize()) {
    HedvigScaffold(
      topAppBarText = stringResource(R.string.PROFILE_PAYMENT_TITLE),
      navigateUp = navigateUp,
      modifier = Modifier
        .fillMaxSize()
        .pullRefresh(pullRefreshState)
        .clearFocusOnTap(),
    ) {
      if (uiState.error != null) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
          HedvigErrorSection(retry = onRetry)
        }
      } else if (uiState.isLoadingPaymentOverView) {
        HedvigFullScreenCenterAlignedProgressDebounced(Modifier.weight(1f))
      } else {
        val memberCharge = uiState.paymentOverview?.memberCharge
        if (memberCharge != null) {
          PaymentAmountCard(
            memberCharge = memberCharge,
            onCardClicked = {
              onUpcomingPaymentClicked(memberCharge, uiState.paymentOverview)
            },
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .animateContentHeight(),
          )
          if (memberCharge.status == MemberCharge.MemberChargeStatus.PENDING) {
            VectorInfoCard(
              text = stringResource(id = R.string.PAYMENTS_IN_PROGRESS),
              modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
          }

          memberCharge.failedCharge?.let {
            VectorErrorCard(
              text = stringResource(
                id = R.string.PAYMENTS_MISSED_PAYMENT,
                dateTimeFormatter.format(it.fromDate.toJavaLocalDate()),
                dateTimeFormatter.format(it.toDate.toJavaLocalDate()),
              ),
              modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
          }
        } else {
          HedvigInformationSection(title = stringResource(id = R.string.PAYMENTS_NO_PAYMENTS_IN_PROGRESS))
        }
        if (uiState.paymentOverview?.paymentConnection?.hasConnectedPayment == false) {
          val text = if (memberCharge != null) {
            stringResource(
              id = R.string.info_card_missing_payment_body_with_date,
              dateTimeFormatter.format(memberCharge.dueDate.toJavaLocalDate()),
            )
          } else {
            stringResource(id = R.string.info_card_missing_payment_body)
          }
          VectorWarningCard(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp),
          ) {
            HedvigContainedSmallButton(
              text = stringResource(id = R.string.PROFILE_PAYMENT_CONNECT_DIRECT_DEBIT_TITLE),
              onClick = onChangeBankAccount,
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.containedButtonContainer,
                contentColor = MaterialTheme.colorScheme.onContainedButtonContainer,
              ),
              textStyle = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.fillMaxWidth(),
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
        }
        uiState.paymentOverview?.let {
          Discounts(
            modifier = Modifier
              .clickable {
                onDiscountClicked(it.discounts)
              }
              .padding(16.dp),
          )
          HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
          PaymentHistory(
            modifier = Modifier
              .clickable {
                onPaymentHistoryClicked(it)
              }
              .padding(16.dp),
          )
        }
        uiState.paymentOverview?.paymentConnection?.connectionInfo?.let {
          HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
          PaymentDetails(
            displayName = it.displayName,
            displayValue = it.displayValue,
          )
        }

        if (uiState.paymentOverview?.paymentConnection?.status == PaymentConnection.PaymentConnectionStatus.PENDING) {
          VectorInfoCard(
            text = stringResource(id = R.string.MY_PAYMENT_UPDATING_MESSAGE),
            modifier = Modifier.padding(horizontal = 16.dp),
          )
        }

        Spacer(Modifier.height(16.dp))
        if (uiState.paymentOverview?.paymentConnection?.hasConnectedPayment == true) {
          HedvigSecondaryContainedButton(
            text = stringResource(R.string.PROFILE_PAYMENT_CHANGE_BANK_ACCOUNT),
            onClick = onChangeBankAccount,
            modifier = Modifier.padding(horizontal = 16.dp),
          )
        }
        Spacer(Modifier.height(16.dp))
      }
    }
    PullRefreshIndicator(
      refreshing = uiState.isRetrying,
      state = pullRefreshState,
      scale = true,
      modifier = Modifier.align(Alignment.TopCenter),
    )
  }
}

@Composable
private fun PaymentAmountCard(memberCharge: MemberCharge, onCardClicked: () -> Unit, modifier: Modifier = Modifier) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()

  HedvigCard(
    onClick = onCardClicked,
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
              text = memberCharge.grossAmount.toString(),
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
      Text(
        text = dateTimeFormatter.format(memberCharge.dueDate.toJavaLocalDate()),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Suppress("UnusedReceiverParameter")
@Composable
private fun ColumnScope.PaymentDetails(displayName: String, displayValue: String) {
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
fun Discounts(modifier: Modifier = Modifier) {
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
fun PaymentHistory(modifier: Modifier = Modifier) {
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
private fun PreviewPaymentScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      PaymentOverviewScreen(
        uiState = OverViewUiState(
          paymentOverview = paymentOverViewPreviewData,
          isLoadingPaymentOverView = true,
        ),
        {},
        { _, _ -> },
        {},
        {},
        {},
      ) {}
    }
  }
}

@Composable
@HedvigPreview
private fun PreviewPaymentScreenNoPayment() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      PaymentOverviewScreen(
        uiState = OverViewUiState(
          paymentOverview = PaymentOverview(
            memberCharge = null,
            pastCharges = null,
            paymentConnection = PaymentConnection(
              connectionInfo = PaymentConnection.ConnectionInfo(
                displayName = "Nordea",
                displayValue = "31489*****",
              ),
              status = PaymentConnection.PaymentConnectionStatus.NEEDS_SETUP,
            ),
            discounts = discountsPreviewData,
          ),
        ),
        {},
        { _, _ -> },
        {},
        {},
        {},
      ) {}
    }
  }
}
