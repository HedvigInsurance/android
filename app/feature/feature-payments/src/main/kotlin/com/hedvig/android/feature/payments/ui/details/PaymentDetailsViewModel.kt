package com.hedvig.android.feature.payments.ui.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.payments.data.GetChargeDetailsUseCase
import com.hedvig.android.feature.payments.data.PaymentDetails
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class PaymentDetailsViewModel(
  chargeId: String?,
  getChargeDetailsUseCase: GetChargeDetailsUseCase,
) : MoleculeViewModel<PaymentDetailsEvent, PaymentDetailsUiState>(
    initialState = PaymentDetailsUiState.Loading,
    presenter = PaymentDetailsPresenter(
      chargeId,
      getChargeDetailsUseCase,
    ),
  )

private class PaymentDetailsPresenter(
  private val chargeId: String?,
  private val getChargeDetailsUseCase: GetChargeDetailsUseCase,
) : MoleculePresenter<PaymentDetailsEvent, PaymentDetailsUiState> {
  @Composable
  override fun MoleculePresenterScope<PaymentDetailsEvent>.present(
    lastState: PaymentDetailsUiState,
  ): PaymentDetailsUiState {
    var dataLoadIteration by remember { mutableIntStateOf(0) }
    var screenState by remember {
      mutableStateOf(lastState)
    }

    CollectEvents {
      when (it) {
        PaymentDetailsEvent.Reload -> dataLoadIteration++
      }
    }

    LaunchedEffect(dataLoadIteration) {
      screenState = PaymentDetailsUiState.Loading
      getChargeDetailsUseCase.invoke(chargeId).fold(
        ifRight = { paymentDetails ->
          screenState = PaymentDetailsUiState.Success(paymentDetails)
        },
        ifLeft = {
          screenState = PaymentDetailsUiState.Failure
        },
      )
    }
    return screenState
  }
}

internal sealed interface PaymentDetailsUiState {
  data object Loading : PaymentDetailsUiState

  data object Failure : PaymentDetailsUiState

  data class Success(
    val paymentDetails: PaymentDetails,
  ) : PaymentDetailsUiState
}

internal sealed interface PaymentDetailsEvent {
  data object Reload : PaymentDetailsEvent
}
