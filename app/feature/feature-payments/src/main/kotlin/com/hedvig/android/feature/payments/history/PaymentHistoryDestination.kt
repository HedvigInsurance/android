package com.hedvig.android.feature.payments.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hedvig.android.core.designsystem.component.information.HedvigInformationSection
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.rememberHedvigMonthDateTimeFormatter
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.feature.payments.data.MemberCharge
import com.hedvig.android.feature.payments.paymentOverViewPreviewData
import hedvig.resources.R
import kotlinx.datetime.toJavaLocalDate

@Composable
internal fun PaymentHistoryDestination(
  pastCharges: List<MemberCharge>?,
  onChargeClicked: (memberCharge: MemberCharge) -> Unit,
  navigateUp: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = stringResource(R.string.PAYMENT_HISTORY_TITLE),
    navigateUp = navigateUp,
  ) {
    if (pastCharges.isNullOrEmpty()) {
      HedvigInformationSection(
        title = stringResource(id = R.string.PAYMENTS_NO_HISTORY_DATA),
        withDefaultVerticalSpacing = true,
      )
      return@HedvigScaffold
    }

    Column {
      Spacer(Modifier.height(8.dp))

      val dateTimeFormatter = rememberHedvigMonthDateTimeFormatter()
      val groupedHistory = pastCharges.reversed().groupBy { it.dueDate.year }
      groupedHistory.forEach {
        val year = it.key
        val charges = it.value

        Text(text = year.toString(), modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(4.dp))

        charges.forEachIndexed { index, charge ->
          HorizontalItemsWithMaximumSpaceTaken(
            startSlot = {
              Text(
                text = dateTimeFormatter.format(charge.dueDate.toJavaLocalDate()),
                color = charge.color(),
              )
            },
            endSlot = {
              Text(
                text = charge.netAmount.toString(),
                color = charge.color(),
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth(),
              )
            },
            modifier = Modifier
              .clickable {
                onChargeClicked(charge)
              }
              .padding(16.dp),
          )

          if (index != charges.size - 1) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
          } else {
            Spacer(Modifier.height(16.dp))
          }
        }
      }

      VectorInfoCard(
        text = stringResource(id = R.string.PAYMENTS_HISTORY_INFO),
        modifier = Modifier.padding(horizontal = 16.dp),
      )
    }
  }
}

@Composable
private fun MemberCharge.color(): Color {
  return if (this.status == MemberCharge.MemberChargeStatus.FAILED) {
    MaterialTheme.colorScheme.error
  } else {
    MaterialTheme.colorScheme.onSurfaceVariant
  }
}

@Composable
@HedvigPreview
internal fun PaymentHistoryScreenPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      PaymentHistoryDestination(
        pastCharges = paymentOverViewPreviewData.pastCharges,
        onChargeClicked = { memberCharge: MemberCharge -> },
        navigateUp = {},
      )
    }
  }
}

@Composable
@HedvigPreview
internal fun PaymentHistoryScreenNoDataPreview() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      PaymentHistoryDestination(
        pastCharges = emptyList(),
        onChargeClicked = { memberCharge: MemberCharge -> },
        navigateUp = {},
      )
    }
  }
}
