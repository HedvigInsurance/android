package com.hedvig.android.feature.payments2

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.payments2.data.GetUpcomingPaymentUseCase
import com.hedvig.android.feature.payments2.data.PaymentOverview
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class PaymentOverviewPresenter(
  val getUpcomingPaymentUseCase: GetUpcomingPaymentUseCase,
) : MoleculePresenter<PaymentEvent, OverViewUiState> {
  @Composable
  override fun MoleculePresenterScope<PaymentEvent>.present(lastState: OverViewUiState): OverViewUiState {
    var paymentUiState: OverViewUiState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
      getUpcomingPaymentUseCase.invoke().fold(
        ifLeft = {
          paymentUiState = OverViewUiState.Error(it.message)
        },
        ifRight = {
          paymentUiState = OverViewUiState.Content(it)
        },
      )
    }

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

    return paymentUiState
  }
}

internal sealed interface PaymentEvent {
  data object Retry : PaymentEvent
}

internal sealed interface OverViewUiState {
  data object Loading : OverViewUiState

  data class Error(
    val errorMessage: String?,
  ) : OverViewUiState

  data class Content(
    val paymentOverview: PaymentOverview,
  ) : OverViewUiState
}
