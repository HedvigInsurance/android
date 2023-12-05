package com.hedvig.android.feature.payments2

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.datetime.LocalDate
import octopus.type.CurrencyCode

internal class PaymentOverviewPresenter : MoleculePresenter<PaymentEvent, OverViewUiState> {
  @Composable
  override fun MoleculePresenterScope<PaymentEvent>.present(lastState: OverViewUiState): OverViewUiState {
    var paymentUiState: OverViewUiState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      if (paymentUiState is OverViewUiState.Error) {
        paymentUiState = OverViewUiState.Loading
      }
    }

    CollectEvents { event ->
      when (event) {
        PaymentEvent.Retry -> loadIteration++
      }
    }

    return OverViewUiState.Content(
      upcomingPayment = UiMoney(1534.0, CurrencyCode.SEK),
      dueDate = LocalDate.fromEpochDays(400),
      connectedPaymentDisplayName = "Nordea",
      connectedPaymentValue = "31489*****",
      hasConnectedPayment = true
    )
  }
}

internal sealed interface PaymentEvent {
  data object Retry : PaymentEvent
}

sealed interface OverViewUiState {
  data object Loading : OverViewUiState
  data class Error(
    val errorMessage: String,
  ) : OverViewUiState

  data class Content(
    val upcomingPayment: UiMoney,
    val dueDate: LocalDate,
    val connectedPaymentDisplayName: String,
    val connectedPaymentValue: String,
    val hasConnectedPayment: Boolean,
  ) : OverViewUiState
}
