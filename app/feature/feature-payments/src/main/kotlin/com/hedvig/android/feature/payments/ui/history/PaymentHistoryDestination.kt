package com.hedvig.android.feature.payments.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.hedvig.android.compose.ui.preview.TripleBooleanCollectionPreviewParameterProvider
import com.hedvig.android.compose.ui.preview.TripleCase
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.design.system.hedvig.DividerPosition
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigInformationSection
import com.hedvig.android.design.system.hedvig.HedvigNotificationCard
import com.hedvig.android.design.system.hedvig.HedvigPreview
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.NotificationDefaults
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.rememberHedvigMonthDateTimeFormatter
import com.hedvig.android.design.system.hedvig.horizontalDivider
import com.hedvig.android.feature.payments.data.MemberCharge
import hedvig.resources.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

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
            PaymentHistory.NoHistoryData to { _: String -> }
          } else {
            PaymentHistory.PastCharges(
              chargesInYear = uiState.paymentHistory.groupBy { it.dueDate.year }.map { (year, charges) ->
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
        Spacer(Modifier.weight(1f))
        HedvigInformationSection(
          title = stringResource(R.string.PAYMENTS_NO_HISTORY_DATA),
          modifier = Modifier.fillMaxSize(),
        )
        Spacer(Modifier.weight(1f))
      }

      is PaymentHistory.PastCharges -> {
        val dateTimeFormatter = rememberHedvigMonthDateTimeFormatter()
        Spacer(Modifier.height(8.dp))
        paymentHistory.chargesInYear.forEachIndexed { yearIndex, yearCharges ->
          if (yearIndex != 0) {
            Spacer(Modifier.height(16.dp))
          }
          HedvigText(text = "${yearCharges.year}", modifier = Modifier.padding(horizontal = 18.dp))
          yearCharges.charge.forEachIndexed { index, charge ->
            HorizontalItemsWithMaximumSpaceTaken(
              startSlot = {
                HedvigText(
                  text = dateTimeFormatter.format(charge.dueDate.toJavaLocalDate()),
                  color = charge.color(),
                  modifier = Modifier.padding(start = 2.dp),
                )
              },
              endSlot = {
                HedvigText(
                  text = charge.netAmount.toString(),
                  color = charge.color(),
                  textAlign = TextAlign.End,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 2.dp),
                )
              },
              modifier = Modifier
                .horizontalDivider(DividerPosition.Top, show = index != 0, horizontalPadding = 16.dp)
                .clickable { onChargeClicked(charge.id) }
                .padding(16.dp),
              spaceBetween = 8.dp,
            )
          }
        }
        if (paymentHistory.showInfoAboutOlderCharges) {
          HedvigNotificationCard(
            message = stringResource(id = R.string.PAYMENTS_HISTORY_INFO),
            priority = NotificationDefaults.NotificationPriority.Info,
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
    HedvigTheme.colorScheme.signalRedElement
  } else {
    HedvigTheme.colorScheme.textSecondary
  }
}

@Composable
@HedvigPreview
internal fun PaymentHistoryScreenPreview(
  @PreviewParameter(
    TripleBooleanCollectionPreviewParameterProvider::class,
  ) cases: TripleCase,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      val buildChargesList: (Int) -> List<PaymentHistory.PastCharges.YearCharges.Charge> = { numberOfCharges ->
        List(numberOfCharges) { index ->
          PaymentHistory.PastCharges.YearCharges.Charge(
            id = "$index",
            dueDate = LocalDate(2021, 7, 1 + index),
            netAmount = UiMoney(index * 100.0, UiCurrencyCode.SEK),
            hasFailedCharge = index % 2 == 0,
          )
        }
      }
      PaymentHistorySuccessScreen(
        paymentHistory = when (cases) {
          TripleCase.FIRST -> PaymentHistory.NoHistoryData
          TripleCase.SECOND -> PaymentHistory.PastCharges(
            listOf(
              PaymentHistory.PastCharges.YearCharges(
                year = 2021,
                charge = buildChargesList(10),
              ),
            ),
            false,
          )

          TripleCase.THIRD -> PaymentHistory.PastCharges(
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
