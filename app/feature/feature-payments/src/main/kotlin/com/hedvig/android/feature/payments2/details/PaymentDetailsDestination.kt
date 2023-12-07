package com.hedvig.android.feature.payments2.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.material3.infoContainer
import com.hedvig.android.core.designsystem.material3.infoElement
import com.hedvig.android.core.designsystem.material3.onInfoContainer
import com.hedvig.android.core.designsystem.material3.onTypeContainer
import com.hedvig.android.core.designsystem.material3.typeContainer
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.icons.Hedvig
import com.hedvig.android.core.icons.hedvig.normal.InfoFilled
import com.hedvig.android.core.icons.hedvig.normal.WarningFilled
import com.hedvig.android.core.icons.hedvig.small.hedvig.Checkmark
import com.hedvig.android.core.ui.rememberHedvigDateTimeFormatter
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.feature.payments2.data.MemberCharge
import com.hedvig.android.feature.payments2.data.PaymentConnection
import com.hedvig.android.feature.payments2.paymentOverViewPreviewData
import hedvig.resources.R
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun PaymentDetailsDestination(
  memberCharge: MemberCharge,
  paymentConnection: PaymentConnection?,
  navigateUp: () -> Unit,
) {
  var selectedCharge by remember { mutableStateOf<MemberCharge.ChargeBreakdown?>(null) }

  MemberChargeDetailsScreen(
    memberCharge = memberCharge,
    paymentConnection = paymentConnection,
    navigateUp = navigateUp,
    selectedCharge = selectedCharge,
    onCardClick = { clickedCharge ->
      selectedCharge = if (selectedCharge == clickedCharge) {
        null
      } else {
        clickedCharge
      }
    },
  )
}

@Composable
internal fun MemberChargeDetailsScreen(
  memberCharge: MemberCharge,
  paymentConnection: PaymentConnection?,
  selectedCharge: MemberCharge.ChargeBreakdown?,
  onCardClick: (MemberCharge.ChargeBreakdown) -> Unit,
  navigateUp: () -> Unit,
) {
  val dateTimeFormatter = rememberHedvigDateTimeFormatter()

  HedvigScaffold(
    topAppBarText = stringResource(R.string.PAYMENTS_UPCOMING_PAYMENT),
    navigateUp = navigateUp,
  ) {
    Column(modifier = Modifier.padding(16.dp)) {

      memberCharge.chargeBreakdowns.forEach { chargeBreakdown ->
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

      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Text(stringResource(id = R.string.payment_details_receipt_card_total))
        },
        endSlot = {
          Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            if (memberCharge.grossAmount != memberCharge.netAmount) {
              Text(
                text = memberCharge.grossAmount.toString(),
                textAlign = TextAlign.End,
                textDecoration = TextDecoration.LineThrough,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Spacer(Modifier.width(6.dp))
            }
            Text(
              text = memberCharge.netAmount.toString(),
              textAlign = TextAlign.End,
            )
          }
        },
        modifier = Modifier.padding(vertical = 16.dp),
      )
      Divider()

      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          Text(stringResource(id = R.string.PAYMENTS_PAYMENT_DUE))
        },
        endSlot = {
          Text(
            text = dateTimeFormatter.format(memberCharge.dueDate.toJavaLocalDate()),
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        },
        modifier = Modifier.padding(vertical = 16.dp),
      )

      when (memberCharge.status) {
        MemberCharge.MemberChargeStatus.UPCOMING -> PaymentStatusCard(
          text = stringResource(id = R.string.PAYMENTS_UPCOMING_PAYMENT),
          icon = Icons.Hedvig.InfoFilled,
          iconColor = MaterialTheme.colorScheme.infoElement,
          colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.infoContainer,
            contentColor = MaterialTheme.colorScheme.onInfoContainer,
          ),
          underTextContent = null,
        )

        MemberCharge.MemberChargeStatus.SUCCESS -> PaymentStatusCard(
          text = stringResource(id = R.string.PAYMENTS_PAYMENT_SUCCESSFUL),
          icon = Icons.Hedvig.Checkmark,
          iconColor = MaterialTheme.colorScheme.onTypeContainer,
          colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.typeContainer,
            contentColor = MaterialTheme.colorScheme.onTypeContainer,
          ),
          underTextContent = null,
        )

        MemberCharge.MemberChargeStatus.PENDING -> PaymentStatusCard(
          text = stringResource(id = R.string.PAYMENTS_IN_PROGRESS),
          icon = Icons.Hedvig.InfoFilled,
          iconColor = MaterialTheme.colorScheme.infoElement,
          colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.infoContainer,
            contentColor = MaterialTheme.colorScheme.onInfoContainer,
          ),
          underTextContent = null,
        )

        MemberCharge.MemberChargeStatus.FAILED -> PaymentStatusCard(
          text = stringResource(
            id = R.string.PAYMENTS_PAYMENT_FAILED, "-",
          ),
          icon = Icons.Hedvig.WarningFilled,
          iconColor = MaterialTheme.colorScheme.error,
          colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
          ),
          underTextContent = null,
        )

        MemberCharge.MemberChargeStatus.UNKNOWN -> {}
      }

      Spacer(Modifier.height(32.dp))

      paymentConnection?.connectionInfo?.let {
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
        Divider()

        HorizontalItemsWithMaximumSpaceTaken(
          startSlot = {
            Text(stringResource(id = R.string.PAYMENTS_ACCOUNT))
          },
          endSlot = {
            Text(
              text = it.displayValue,
              textAlign = TextAlign.End,
              modifier = Modifier.fillMaxWidth(),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          modifier = Modifier.padding(vertical = 16.dp),
        )
        Divider()

        HorizontalItemsWithMaximumSpaceTaken(
          startSlot = { Text(stringResource(id = R.string.PAYMENTS_DUE_LABEL)) },
          endSlot = {
            Text(
              text = it.displayName,
              textAlign = TextAlign.End,
              modifier = Modifier.fillMaxWidth(),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          modifier = Modifier.padding(vertical = 16.dp),
        )
        Divider()
      }
    }
  }
}

@Composable
@HedvigPreview
internal fun PaymentDetailsScreenPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      MemberChargeDetailsScreen(
        memberCharge = paymentOverViewPreviewData.memberCharge!!,
        paymentConnection = paymentOverViewPreviewData.paymentConnection!!,
        selectedCharge = null,
        onCardClick = {},
        navigateUp = {},
      )
    }
  }
}
