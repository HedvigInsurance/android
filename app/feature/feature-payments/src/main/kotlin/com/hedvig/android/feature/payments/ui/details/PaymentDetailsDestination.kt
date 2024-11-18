package com.hedvig.android.feature.payments.ui.details

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Icon
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBarColors
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigDateTimeFormatter
import com.hedvig.android.design.system.hedvig.icon.HedvigIcons
import com.hedvig.android.design.system.hedvig.icon.InfoFilled
import com.hedvig.android.design.system.hedvig.icon.WarningFilled
import com.hedvig.android.feature.payments.chargeHistoryPreviewData
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.data.PaymentConnection
import com.hedvig.android.feature.payments.data.PaymentDetails
import com.hedvig.android.feature.payments.paymentDetailsPreviewData
import com.hedvig.android.feature.payments.paymentOverViewPreviewData
import com.hedvig.android.feature.payments.ui.discounts.DiscountRows
import hedvig.resources.R
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun PaymentDetailsDestination(
  viewModel: PaymentDetailsViewModel,
  onFailedChargeClick: (String?) -> Unit,
  navigateUp: () -> Unit,
) {
  var selectedCharge by remember { mutableStateOf<MemberCharge.ChargeBreakdown?>(null) }
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  MemberChargeDetailsScreen(
    uiState = uiState,
    navigateUp = navigateUp,
    selectedCharge = selectedCharge,
    onCardClick = { clickedCharge ->
      selectedCharge = if (selectedCharge == clickedCharge) {
        null
      } else {
        clickedCharge
      }
    },
    onFailedChargeClick = onFailedChargeClick,
    reload = { viewModel.emit(PaymentDetailsEvent.Reload) },
  )
}

@Composable
private fun MemberChargeDetailsScreen(
  uiState: PaymentDetailsUiState,
  selectedCharge: MemberCharge.ChargeBreakdown?,
  onCardClick: (MemberCharge.ChargeBreakdown) -> Unit,
  reload: () -> Unit,
  onFailedChargeClick: (String?) -> Unit,
  navigateUp: () -> Unit,
) {
  when (uiState) {
    PaymentDetailsUiState.Failure -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
      }
    }
    PaymentDetailsUiState.Loading -> HedvigFullScreenCenterAlignedProgress()
    is PaymentDetailsUiState.Success -> {
      val dateTimeFormatter = rememberHedvigDateTimeFormatter()

      HedvigScaffold(
        topAppBarText = dateTimeFormatter.format(uiState.paymentDetails.memberCharge.dueDate.toJavaLocalDate()),
        navigateUp = navigateUp,
        customTopAppBarColors = uiState.paymentDetails.memberCharge.topAppBarColors(),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          var showBottomSheet by remember { mutableStateOf(false) }
          if (showBottomSheet) {
            HedvigInfoBottomSheet(
              onDismissed = { showBottomSheet = false },
              title = stringResource(id = R.string.PAYMENTS_PAYMENT_DETAILS_INFO_TITLE),
              body = stringResource(id = R.string.PAYMENTS_PAYMENT_DETAILS_INFO_DESCRIPTION),
            )
          }
          uiState.paymentDetails.memberCharge.chargeBreakdowns.forEach { chargeBreakdown ->
            PaymentDetailExpandableCard(
              displayName = chargeBreakdown.contractDisplayName,
              subtitle = chargeBreakdown.contractDetails,
              totalAmount = chargeBreakdown.grossAmount.toString(),
              periods = chargeBreakdown.periods,
              isExpanded = selectedCharge == chargeBreakdown,
              onClick = { onCardClick(chargeBreakdown) },
            )
            Spacer(modifier = Modifier.height(8.dp))
          }

          uiState.paymentDetails.memberCharge.carriedAdjustmentIfAboveZero()?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              HedvigText(text = stringResource(id = R.string.payments_carried_adjustment))
              HedvigText(text = it.toString())
            }
            Spacer(modifier = Modifier.height(8.dp))
            HedvigNotificationCard(
              priority = NotificationDefaults.NotificationPriority.Info,
              message = stringResource(id = R.string.payments_carried_adjustment_info))
            Spacer(modifier = Modifier.height(16.dp))
          }

          uiState.paymentDetails.memberCharge.settlementAdjustmentIfAboveZero()?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              HedvigText(text = stringResource(id = R.string.payments_settlement_adjustment))
              HedvigText(text = it.toString())
            }
            Spacer(modifier = Modifier.height(8.dp))
            HedvigNotificationCard(
              priority = NotificationDefaults.NotificationPriority.Info,
              message  = stringResource(id = R.string.payments_settlement_adjustment_info))
            Spacer(modifier = Modifier.height(16.dp))
          }

          if (uiState.paymentDetails.memberCharge.discounts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            HedvigText(stringResource(R.string.PAYMENTS_DISCOUNTS_SECTION_TITLE))
            Spacer(modifier = Modifier.height(16.dp))
            DiscountRows(uiState.paymentDetails.memberCharge.discounts)
            Spacer(modifier = Modifier.height(16.dp))
          }

          HorizontalItemsWithMaximumSpaceTaken(
            startSlot = {
              HedvigText(stringResource(R.string.payment_details_receipt_card_total))
            },
            endSlot = {
              Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                if (uiState.paymentDetails.memberCharge.grossAmount != uiState.paymentDetails.memberCharge.netAmount) {
                  HedvigText(
                    text = uiState.paymentDetails.memberCharge.grossAmount.toString(),
                    textAlign = TextAlign.End,
                    textDecoration = TextDecoration.LineThrough,
                    color = HedvigTheme.colorScheme.textSecondary,
                  )
                  Spacer(Modifier.width(6.dp))
                }
                HedvigText(
                  text = uiState.paymentDetails.memberCharge.netAmount.toString(),
                  textAlign = TextAlign.End,
                )
              }
            },
          )
          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider()

          HorizontalItemsWithMaximumSpaceTaken(
            startSlot = {
              HedvigText(stringResource(id = R.string.PAYMENTS_PAYMENT_DUE))
            },
            endSlot = {
              HedvigText(
                text = dateTimeFormatter.format(uiState.paymentDetails.memberCharge.dueDate.toJavaLocalDate()),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                color =  HedvigTheme.colorScheme.textSecondary,
              )
            },
            modifier = Modifier.padding(vertical = 16.dp),
          )

          when (uiState.paymentDetails.memberCharge.status) {
            MemberCharge.MemberChargeStatus.UPCOMING -> {}
            MemberCharge.MemberChargeStatus.SUCCESS ->
              HedvigNotificationCard(
                priority = NotificationDefaults.NotificationPriority.Campaign,
                //so here we have the same color that was in the old DS - green; but the icon doesn't match (it was checkmark,
                //we don't have Checkmark icon in the new NotificationCards. so I've put  withIcon = false here
                withIcon = false,
                style = NotificationDefaults.InfoCardStyle.Default,
                message = stringResource(id = R.string.PAYMENTS_PAYMENT_SUCCESSFUL)
              )


            MemberCharge.MemberChargeStatus.PENDING -> HedvigNotificationCard(
              message = stringResource(id = R.string.PAYMENTS_IN_PROGRESS),
              style = NotificationDefaults.InfoCardStyle.Default,
              priority = NotificationDefaults.NotificationPriority.Info,
              withIcon = true,
            )

            MemberCharge.MemberChargeStatus.FAILED -> HedvigNotificationCard(
              message = stringResource(
                id = R.string.PAYMENTS_PAYMENT_FAILED,
                uiState.paymentDetails.getNextCharge(uiState.paymentDetails.memberCharge)?.let {
                  dateTimeFormatter.format(it.dueDate.toJavaLocalDate())
                } ?: "-",
              ),
              priority = NotificationDefaults.NotificationPriority.Attention,
              withIcon = true,
              style = NotificationDefaults.InfoCardStyle.Button(
                buttonText = stringResource(R.string.PAYMENTS_VIEW_PAYMENT),
                onButtonClick = {
                  val nextCharge = uiState.paymentDetails.getNextCharge(uiState.paymentDetails.memberCharge)
                  if (nextCharge != null) {
                    onFailedChargeClick(nextCharge.id)
                  }
                },
              ),
            )

            MemberCharge.MemberChargeStatus.UNKNOWN -> {}
          }

          val paymentConnection = uiState.paymentDetails.paymentConnection
          if (paymentConnection is PaymentConnection.Active) {
            Spacer(Modifier.height(32.dp))
            HorizontalItemsWithMaximumSpaceTaken(
              startSlot = {
                HedvigText(stringResource(id = R.string.PAYMENTS_PAYMENT_DETAILS_INFO_TITLE))
              },
              endSlot = {
                Box(
                  contentAlignment = Alignment.CenterEnd,
                  modifier = Modifier
                    .fillMaxWidth(),
                ) {
                  Box(
                    modifier = Modifier
                      .fillMaxHeight()
                      .width(32.dp)
                      .clip(HedvigTheme.shapes.cornerMedium)
                      .clickable { showBottomSheet = true },
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector =HedvigIcons.InfoFilled,
                      tint =  HedvigTheme.colorScheme.fillSecondary,
                      contentDescription = "Info icon",
                      modifier = Modifier.size(16.dp),
                    )
                  }
                }
              },
              modifier = Modifier.padding(vertical = 16.dp),
            )
            HorizontalDivider()

            HorizontalItemsWithMaximumSpaceTaken(
              startSlot = {
                HedvigText(stringResource(id = R.string.PAYMENTS_PAYMENT_METHOD))
              },
              endSlot = {
                HedvigText(
                  text = stringResource(id = R.string.PAYMENTS_AUTOGIRO_LABEL),
                  textAlign = TextAlign.End,
                  modifier = Modifier.fillMaxWidth(),
                  color = HedvigTheme.colorScheme.textSecondary,
                )
              },
              modifier = Modifier.padding(vertical = 16.dp),
            )
            HorizontalDivider()

            HorizontalItemsWithMaximumSpaceTaken(
              startSlot = {
                HedvigText(stringResource(id = R.string.PAYMENTS_ACCOUNT))
              },
              endSlot = {
                HedvigText(
                  text = paymentConnection.displayValue,
                  textAlign = TextAlign.End,
                  modifier = Modifier.fillMaxWidth(),
                  color =  HedvigTheme.colorScheme.textSecondary,
                )
              },
              modifier = Modifier.padding(vertical = 16.dp),
            )
            HorizontalDivider()

            HorizontalItemsWithMaximumSpaceTaken(
              startSlot = { HedvigText(stringResource(id = R.string.PAYMENTS_BANK_LABEL)) },
              endSlot = {
                HedvigText(
                  text = paymentConnection.displayName,
                  textAlign = TextAlign.End,
                  modifier = Modifier.fillMaxWidth(),
                  color =  HedvigTheme.colorScheme.textSecondary,
                )
              },
              modifier = Modifier.padding(vertical = 16.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MemberCharge.topAppBarColors(): TopAppBarColors? {
  return if (this.status == MemberCharge.MemberChargeStatus.FAILED) {
    TopAppBarColors(
      contentColor = HedvigTheme.colorScheme.backgroundPrimary,
      containerColor = HedvigTheme.colorScheme.signalRedText
    )
  } else null
}

@Composable
@Preview(device = "spec:width=1080px,height=3500px,dpi=440")
@HedvigPreview
private fun PaymentDetailsScreenPreview() {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      MemberChargeDetailsScreen(
        uiState = PaymentDetailsUiState.Success(
          PaymentDetails(
            memberCharge = paymentDetailsPreviewData,
            pastCharges = chargeHistoryPreviewData,
            paymentConnection = paymentOverViewPreviewData.paymentConnection,
          ),
        ),
        selectedCharge = null,
        onCardClick = {},
        navigateUp = {},
        onFailedChargeClick = {},
        reload = {},
      )
    }
  }
}
