package com.hedvig.android.feature.payments.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.payments.data.ChargeHistory
import com.hedvig.android.feature.payments.data.PaymentRepository
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class PaymentHistoryViewModel(
  private val paymentRepositoryProvider: Provider<PaymentRepository>,
) : MoleculeViewModel<PaymentHistoryEvent, PaymentHistoryUiState>(
    PaymentHistoryUiState.Loading,
    PaymentHistoryPresenter(paymentRepositoryProvider),
  )

internal sealed interface PaymentHistoryUiState {
  data object Loading : PaymentHistoryUiState

  data class Error(val errorMessage: String?) : PaymentHistoryUiState

  data object NoChargeHistory : PaymentHistoryUiState

  data class Content(val chargeHistory: ChargeHistory, val isLoading: Boolean) : PaymentHistoryUiState
}

internal sealed interface PaymentHistoryEvent {
  data object Retry : PaymentHistoryEvent
}

private class PaymentHistoryPresenter(
  private val paymentRepositoryProvider: Provider<PaymentRepository>,
) : MoleculePresenter<PaymentHistoryEvent, PaymentHistoryUiState> {
  @Composable
  override fun MoleculePresenterScope<PaymentHistoryEvent>.present(
    lastState: PaymentHistoryUiState,
  ): PaymentHistoryUiState {
    var chargeHistory: ChargeHistory? by remember {
      mutableStateOf((lastState as? PaymentHistoryUiState.Content)?.chargeHistory)
    }
    var isLoading: Boolean by remember {
      val isLoadingWithContent = lastState is PaymentHistoryUiState.Content && lastState.isLoading
      mutableStateOf(lastState is PaymentHistoryUiState.Loading || isLoadingWithContent)
    }
    var errorMessage: ErrorMessage? by remember { mutableStateOf(null) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      isLoading = true
      paymentRepositoryProvider.provide().getChargeHistory().fold(
        ifLeft = {
          chargeHistory = null
          errorMessage = it
        },
        ifRight = {
          chargeHistory = it
          errorMessage = null
        },
      )
      isLoading = false
    }

    CollectEvents { event ->
      when (event) {
        is PaymentHistoryEvent.Retry -> loadIteration++
      }
    }

    val chargeHistoryValue = chargeHistory
    return when {
      errorMessage != null -> PaymentHistoryUiState.Error(errorMessage?.message)
      chargeHistoryValue == null -> PaymentHistoryUiState.Loading
      chargeHistoryValue.charges.isEmpty() -> PaymentHistoryUiState.NoChargeHistory
      else -> PaymentHistoryUiState.Content(
        chargeHistory = chargeHistoryValue,
        isLoading = isLoading,
      )
    }
  }
}
