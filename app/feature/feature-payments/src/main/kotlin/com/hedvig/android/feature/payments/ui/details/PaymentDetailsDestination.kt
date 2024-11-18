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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
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
import com.hedvig.android.core.designsystem.component.bottomsheet.HedvigInfoBottomSheet
import com.hedvig.android.core.designsystem.component.button.HedvigContainedSmallButton
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.material3.containedButtonContainer
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.infoElement
import com.hedvig.android.core.designsystem.material3.onContainedButtonContainer
import com.hedvig.android.core.designsystem.material3.onInfoContainer
import com.hedvig.android.core.designsystem.material3.onTypeContainer
import com.hedvig.android.core.designsystem.material3.squircleMedium
import com.hedvig.android.core.designsystem.material3.typeContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.InfoFilled
import com.hedvig.android.core.icons.hedvig.normal.WarningFilled
import com.hedvig.android.core.icons.hedvig.small.hedvig.Checkmark
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
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
        topAppBarColors = uiState.paymentDetails.memberCharge.topAppBarColors(),
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
              Text(text = stringResource(id = R.string.payments_carried_adjustment))
              Text(text = it.toString())
            }
            Spacer(modifier = Modifier.height(8.dp))
            VectorInfoCard(text = stringResource(id = R.string.payments_carried_adjustment_info))
            Spacer(modifier = Modifier.height(16.dp))
          }

          uiState.paymentDetails.memberCharge.settlementAdjustmentIfAboveZero()?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(text = stringResource(id = R.string.payments_settlement_adjustment))
              Text(text = it.toString())
            }
            Spacer(modifier = Modifier.height(8.dp))
            VectorInfoCard(text = stringResource(id = R.string.payments_settlement_adjustment_info))
            Spacer(modifier = Modifier.height(16.dp))
          }

          if (uiState.paymentDetails.memberCharge.discounts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.PAYMENTS_DISCOUNTS_SECTION_TITLE))
            Spacer(modifier = Modifier.height(16.dp))
            DiscountRows(uiState.paymentDetails.memberCharge.discounts)
            Spacer(modifier = Modifier.height(16.dp))
          }

          HorizontalItemsWithMaximumSpaceTaken(
            startSlot = {
              Text(stringResource(R.string.payment_details_receipt_card_total))
            },
            endSlot = {
              Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                if (uiState.paymentDetails.memberCharge.grossAmount != uiState.paymentDetails.memberCharge.netAmount) {
                  Text(
                    text = uiState.paymentDetails.memberCharge.grossAmount.toString(),
                    textAlign = TextAlign.End,
                    textDecoration = TextDecoration.LineThrough,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                  Spacer(Modifier.width(6.dp))
                }
                Text(
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
              Text(stringResource(id = R.string.PAYMENTS_PAYMENT_DUE))
            },
            endSlot = {
              Text(
                text = dateTimeFormatter.format(uiState.paymentDetails.memberCharge.dueDate.toJavaLocalDate()),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            },
            modifier = Modifier.padding(vertical = 16.dp),
          )

          when (uiState.paymentDetails.memberCharge.status) {
            MemberCharge.MemberChargeStatus.UPCOMING -> {}
            MemberCharge.MemberChargeStatus.SUCCESS -> VectorInfoCard(
              text = stringResource(id = R.string.PAYMENTS_PAYMENT_SUCCESSFUL),
              icon = Icons.Hedvig.Checkmark,
              iconColor = MaterialTheme.colorScheme.onTypeContainer,
              colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.typeContainer,
                contentColor = MaterialTheme.colorScheme.onTypeContainer,
              ),
              underTextContent = null,
            )

            MemberCharge.MemberChargeStatus.PENDING -> VectorInfoCard(
              text = stringResource(id = R.string.PAYMENTS_IN_PROGRESS),
              icon = Icons.Hedvig.InfoFilled,
              iconColor = MaterialTheme.colorScheme.infoElement,
              colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.infoContainer,
                contentColor = MaterialTheme.colorScheme.onInfoContainer,
              ),
              underTextContent = null,
            )

            MemberCharge.MemberChargeStatus.FAILED -> VectorInfoCard(
              text = stringResource(
                id = R.string.PAYMENTS_PAYMENT_FAILED,
                uiState.paymentDetails.getNextCharge(uiState.paymentDetails.memberCharge)?.let {
                  dateTimeFormatter.format(it.dueDate.toJavaLocalDate())
                } ?: "-",
              ),
              icon = Icons.Hedvig.WarningFilled,
              iconColor = MaterialTheme.colorScheme.error,
              colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
              ),
              underTextContent = {
                HedvigContainedSmallButton(
                  text = stringResource(R.string.PAYMENTS_VIEW_PAYMENT),
                  onClick = {
                    val nextCharge = uiState.paymentDetails.getNextCharge(uiState.paymentDetails.memberCharge)
                    if (nextCharge != null) {
                      onFailedChargeClick(nextCharge.id)
                    }
                  },
                  colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.containedButtonContainer,
                    contentColor = MaterialTheme.colorScheme.onContainedButtonContainer,
                  ),
                  textStyle = MaterialTheme.typography.bodyMedium,
                  modifier = Modifier.fillMaxWidth(),
                )
              },
            )

            MemberCharge.MemberChargeStatus.UNKNOWN -> {}
          }

          val paymentConnection = uiState.paymentDetails.paymentConnection
          if (paymentConnection is PaymentConnection.Active) {
            Spacer(Modifier.height(32.dp))
            HorizontalItemsWithMaximumSpaceTaken(
              startSlot = {
                Text(stringResource(id = R.string.PAYMENTS_PAYMENT_DETAILS_INFO_TITLE))
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
                      .clip(MaterialTheme.shapes.squircleMedium)
                      .clickable { showBottomSheet = true },
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(
                      imageVector = Icons.Hedvig.InfoFilled,
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                Text(stringResource(id = R.string.PAYMENTS_PAYMENT_METHOD))
              },
              endSlot = {
                Text(
                  text = stringResource(id = R.string.PAYMENTS_AUTOGIRO_LABEL),
                  textAlign = TextAlign.End,
                  modifier = Modifier.fillMaxWidth(),
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              },
              modifier = Modifier.padding(vertical = 16.dp),
            )
            HorizontalDivider()

            HorizontalItemsWithMaximumSpaceTaken(
              startSlot = {
                Text(stringResource(id = R.string.PAYMENTS_ACCOUNT))
              },
              endSlot = {
                Text(
                  text = paymentConnection.displayValue,
                  textAlign = TextAlign.End,
                  modifier = Modifier.fillMaxWidth(),
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              },
              modifier = Modifier.padding(vertical = 16.dp),
            )
            HorizontalDivider()

            HorizontalItemsWithMaximumSpaceTaken(
              startSlot = { Text(stringResource(id = R.string.PAYMENTS_BANK_LABEL)) },
              endSlot = {
                Text(
                  text = paymentConnection.displayName,
                  textAlign = TextAlign.End,
                  modifier = Modifier.fillMaxWidth(),
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun MemberCharge.topAppBarColors(): TopAppBarColors {
  return if (this.status == MemberCharge.MemberChargeStatus.FAILED) {
    TopAppBarDefaults.topAppBarColors(
      titleContentColor = MaterialTheme.colorScheme.error,
      containerColor = MaterialTheme.colorScheme.background,
      scrolledContainerColor = MaterialTheme.colorScheme.surface,
    )
  } else {
    TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.background,
      scrolledContainerColor = MaterialTheme.colorScheme.surface,
    )
  }
}

@Composable
@Preview(device = "spec:width=1080px,height=3500px,dpi=440")
@HedvigPreview
private fun PaymentDetailsScreenPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
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
