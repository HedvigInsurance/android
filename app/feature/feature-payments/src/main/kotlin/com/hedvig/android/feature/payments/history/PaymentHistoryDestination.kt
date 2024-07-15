package com.hedvig.android.feature.payments.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.information.HedvigInformationSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.infocard.VectorInfoCard
import com.hedvig.android.core.ui.rememberHedvigMonthDateTimeFormatter
import com.hedvig.android.core.ui.scaffold.HedvigScaffold
import com.hedvig.android.core.ui.text.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.MemberCharge
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import octopus.type.CurrencyCode

@Composable
internal fun PaymentHistoryDestination(
  viewModel: PaymentHistoryViewModel,
  onChargeClicked: (memberChargeId: String) -> Unit,
  navigateUp: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  PaymentHistoryScreen(
    uiState = uiState,
    onChargeClicked = onChargeClicked,
    navigateUp = navigateUp,
    reload = { viewModel.emit(PaymentHistoryEvent.Reload) },
  )
}

@Composable
private fun PaymentHistoryScreen(
  uiState: PaymentHistoryUiState,
  onChargeClicked: (chargeId: String) -> Unit,
  navigateUp: () -> Unit,
  reload: () -> Unit,
) {
  when (uiState) {
    PaymentHistoryUiState.Failure -> {
      HedvigScaffold(
        navigateUp = navigateUp,
      ) {
        HedvigErrorSection(onButtonClick = reload, modifier = Modifier.weight(1f))
      }
    }

    PaymentHistoryUiState.Loading -> HedvigFullScreenCenterAlignedProgress()
    is PaymentHistoryUiState.Success -> {
      val updatedOnChargeClicked by rememberUpdatedState(onChargeClicked)
      val (paymentHistory: PaymentHistory, onChargeClickedAfterTransform: (String) -> Unit) =
        remember(uiState.paymentHistory) {
          if (uiState.paymentHistory.isEmpty()) {
            PaymentHistory.NoHistoryData to { _ -> }
          } else {
            PaymentHistory.PastCharges(
              chargesInYear = uiState.paymentHistory.sortedBy { it.dueDate }.groupBy { it.dueDate.year }
                .map { (year, charges) ->
                  PaymentHistory.PastCharges.YearCharges(
                    year = year,
                    charge = charges.map { charge ->
                      PaymentHistory.PastCharges.YearCharges.Charge(
                        id = charge.id,
                        dueDate = charge.dueDate,
                        netAmount = charge.netAmount,
                        hasFailedCharge = charge.status == MemberCharge.MemberChargeStatus.FAILED,
                      )
                    },
                  )
                },
              showInfoAboutOlderCharges = uiState.paymentHistory.size > 11,
            ) to { chargeId: String ->
              updatedOnChargeClicked(uiState.paymentHistory.first { it.id == chargeId }.id)
            }
          }
        }
      PaymentHistorySuccessScreen(
        paymentHistory = paymentHistory,
        onChargeClicked = onChargeClickedAfterTransform,
        navigateUp = navigateUp,
      )
    }
  }
}

@Composable
private fun PaymentHistorySuccessScreen(
  paymentHistory: PaymentHistory,
  onChargeClicked: (chargeId: String) -> Unit,
  navigateUp: () -> Unit,
) {
  HedvigScaffold(
    topAppBarText = stringResource(R.string.PAYMENT_HISTORY_TITLE),
    navigateUp = navigateUp,
  ) {
    when (paymentHistory) {
      PaymentHistory.NoHistoryData -> {
        HedvigInformationSection(
          title = stringResource(R.string.PAYMENTS_NO_HISTORY_DATA),
          withDefaultVerticalSpacing = true,
          modifier = Modifier.weight(1f),
        )
      }

      is PaymentHistory.PastCharges -> {
        val dateTimeFormatter = rememberHedvigMonthDateTimeFormatter()
        Spacer(Modifier.height(8.dp))
        paymentHistory.chargesInYear.forEachIndexed { yearIndex, yearCharges ->
          if (yearIndex != 0) {
            Spacer(Modifier.height(16.dp))
          }
          Text(text = "${yearCharges.year}", modifier = Modifier.padding(horizontal = 18.dp))
          yearCharges.charge.forEachIndexed { chargeIndex, charge ->
            if (chargeIndex != 0) {
              HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            HorizontalItemsWithMaximumSpaceTaken(
              startSlot = {
                Text(
                  text = dateTimeFormatter.format(charge.dueDate.toJavaLocalDate()),
                  color = charge.color(),
                  modifier = Modifier.padding(start = 2.dp),
                )
              },
              endSlot = {
                Text(
                  text = charge.netAmount.toString(),
                  color = charge.color(),
                  textAlign = TextAlign.End,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 2.dp),
                )
              },
              modifier = Modifier
                .clickable { onChargeClicked(charge.id) }
                .padding(16.dp),
            )
          }
        }
        if (paymentHistory.showInfoAboutOlderCharges) {
          VectorInfoCard(
            text = stringResource(id = R.string.PAYMENTS_HISTORY_INFO),
            modifier = Modifier.padding(horizontal = 16.dp),
          )
        }
        Spacer(Modifier.height(16.dp))
      }
    }
  }
}

private sealed interface PaymentHistory {
  object NoHistoryData : PaymentHistory

  data class PastCharges(
    val chargesInYear: List<YearCharges>,
    val showInfoAboutOlderCharges: Boolean,
  ) : PaymentHistory {
    data class YearCharges(
      val year: Int,
      val charge: List<Charge>,
    ) {
      data class Charge(
        val id: String,
        val dueDate: LocalDate,
        val netAmount: UiMoney,
        val hasFailedCharge: Boolean,
      )
    }
  }
}

@Composable
private fun PaymentHistory.PastCharges.YearCharges.Charge.color(): Color {
  return if (hasFailedCharge) {
    MaterialTheme.colorScheme.error
  } else {
    MaterialTheme.colorScheme.onSurfaceVariant
  }
}

@Composable
@HedvigPreview
internal fun PaymentHistoryScreenPreview(
  @PreviewParameter(
    com.hedvig.android.compose.ui.preview.TripleBooleanCollectionPreviewParameterProvider::class,
  ) cases: com.hedvig.android.compose.ui.preview.TripleCase,
) {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      val buildChargesList: (Int) -> List<PaymentHistory.PastCharges.YearCharges.Charge> = { numberOfCharges ->
        List(numberOfCharges) { index ->
          PaymentHistory.PastCharges.YearCharges.Charge(
            id = "$index",
            dueDate = LocalDate(2021, 7, 1 + index),
            netAmount = UiMoney(index * 100.0, CurrencyCode.SEK),
            hasFailedCharge = index % 2 == 0,
          )
        }
      }
      PaymentHistorySuccessScreen(
        paymentHistory = when (cases) {
          com.hedvig.android.compose.ui.preview.TripleCase.FIRST -> PaymentHistory.NoHistoryData
          com.hedvig.android.compose.ui.preview.TripleCase.SECOND -> PaymentHistory.PastCharges(
            listOf(
              PaymentHistory.PastCharges.YearCharges(
                year = 2021,
                charge = buildChargesList(10),
              ),
            ),
            false,
          )

          com.hedvig.android.compose.ui.preview.TripleCase.THIRD -> PaymentHistory.PastCharges(
            List(3) {
              PaymentHistory.PastCharges.YearCharges(
                year = 2021 + it,
                charge = buildChargesList(3),
              )
            },
            true,
          )
        },
        onChargeClicked = {},
        navigateUp = {},
      )
    }
  }
}
