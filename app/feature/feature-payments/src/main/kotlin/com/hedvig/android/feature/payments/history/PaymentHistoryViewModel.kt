package com.hedvig.android.feature.payments.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.payments.data.GetPaymentsHistoryUseCase
import com.hedvig.android.feature.payments.data.PaymentHistoryItem
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class PaymentHistoryViewModel(
  getPaymentsHistoryUseCase: GetPaymentsHistoryUseCase,
) : MoleculeViewModel<PaymentHistoryEvent, PaymentHistoryUiState>(
    initialState = PaymentHistoryUiState.Loading,
    presenter = PaymentHistoryPresenter(getPaymentsHistoryUseCase),
  )

private class PaymentHistoryPresenter(
  private val getPaymentsHistoryUseCase: GetPaymentsHistoryUseCase,
) : MoleculePresenter<PaymentHistoryEvent, PaymentHistoryUiState> {
  @Composable
  override fun MoleculePresenterScope<PaymentHistoryEvent>.present(
    lastState: PaymentHistoryUiState,
  ): PaymentHistoryUiState {
    var dataLoadIteration by remember { mutableIntStateOf(0) }
    var screenState by remember {
      mutableStateOf(lastState)
    }

    CollectEvents {
      when (it) {
        PaymentHistoryEvent.Reload -> dataLoadIteration++
      }
    }

    LaunchedEffect(dataLoadIteration) {
      screenState = PaymentHistoryUiState.Loading
      getPaymentsHistoryUseCase.invoke().fold(
        ifLeft = {
          screenState = PaymentHistoryUiState.Failure
        },
        ifRight = { list ->
          screenState = PaymentHistoryUiState.Success(list)
        },
      )
    }

    return screenState
  }
}

internal sealed interface PaymentHistoryUiState {
  data object Loading : PaymentHistoryUiState

  data object Failure : PaymentHistoryUiState

  data class Success(val paymentHistory: List<PaymentHistoryItem>) : PaymentHistoryUiState
}

internal sealed interface PaymentHistoryEvent {
  data object Reload : PaymentHistoryEvent
}
