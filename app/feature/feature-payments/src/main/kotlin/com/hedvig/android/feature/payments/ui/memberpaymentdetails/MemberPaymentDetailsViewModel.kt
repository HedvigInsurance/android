package com.hedvig.android.feature.payments.ui.memberpaymentdetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.payments.data.GetMemberPaymentsDetailsUseCase
import com.hedvig.android.feature.payments.data.MemberPaymentsDetails
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class MemberPaymentDetailsViewModel(
  getMemberPaymentsDetailsUseCase: GetMemberPaymentsDetailsUseCase,
) : MoleculeViewModel<MemberPaymentDetailsEvent, MemberPaymentDetailsUiState>(
  initialState = MemberPaymentDetailsUiState.Loading,
  presenter = MemberPaymentDetailsPresenter(
    getMemberPaymentsDetailsUseCase,
  ),
)

private class MemberPaymentDetailsPresenter(
  private val getMemberPaymentsDetailsUseCase: GetMemberPaymentsDetailsUseCase,
) : MoleculePresenter<MemberPaymentDetailsEvent, MemberPaymentDetailsUiState> {
  @Composable
  override fun MoleculePresenterScope<MemberPaymentDetailsEvent>.present(
    lastState: MemberPaymentDetailsUiState,
  ): MemberPaymentDetailsUiState {
    var dataLoadIteration by remember { mutableIntStateOf(0) }
    var currentState by remember {
      mutableStateOf(lastState)
    }

    CollectEvents {
      when (it) {
        MemberPaymentDetailsEvent.Retry -> TODO()
      }
    }

    LaunchedEffect(dataLoadIteration) {
      currentState = MemberPaymentDetailsUiState.Loading
      getMemberPaymentsDetailsUseCase.invoke().fold(
        ifLeft = {
          currentState = MemberPaymentDetailsUiState.Failure
        },
        ifRight = {
          currentState = MemberPaymentDetailsUiState.Success(it)
        },
      )
    }
    return currentState
  }
}

internal sealed interface MemberPaymentDetailsUiState {
  data object Loading : MemberPaymentDetailsUiState

  data object Failure : MemberPaymentDetailsUiState

  data class Success(
    val paymentDetails: MemberPaymentsDetails,
  ) : MemberPaymentDetailsUiState
}

internal sealed interface MemberPaymentDetailsEvent {
  data object Retry: MemberPaymentDetailsEvent
  //todo
}
