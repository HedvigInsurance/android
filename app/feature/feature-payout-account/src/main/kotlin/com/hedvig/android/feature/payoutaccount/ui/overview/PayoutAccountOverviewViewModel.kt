package com.hedvig.android.feature.payoutaccount.ui.overview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.payoutaccount.data.GetPayoutAccountUseCase
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import octopus.type.MemberPaymentProvider

internal class PayoutAccountOverviewViewModel(
  getPayoutAccountUseCase: GetPayoutAccountUseCase,
) : MoleculeViewModel<PayoutAccountOverviewEvent, PayoutAccountOverviewUiState>(
    PayoutAccountOverviewUiState.Loading,
    PayoutAccountOverviewPresenter(getPayoutAccountUseCase),
  )

internal sealed interface PayoutAccountOverviewEvent {
  data object Retry : PayoutAccountOverviewEvent
}

internal sealed interface PayoutAccountOverviewUiState {
  data object Loading : PayoutAccountOverviewUiState

  data object Error : PayoutAccountOverviewUiState

  data object NoPayoutOptions : PayoutAccountOverviewUiState

  data class Content(
    val currentMethod: PayoutAccount?,
    val availablePayoutMethods: List<MemberPaymentProvider>,
  ) : PayoutAccountOverviewUiState
}

internal class PayoutAccountOverviewPresenter(
  private val getPayoutAccountUseCase: GetPayoutAccountUseCase,
) : MoleculePresenter<PayoutAccountOverviewEvent, PayoutAccountOverviewUiState> {
  @Composable
  override fun MoleculePresenterScope<PayoutAccountOverviewEvent>.present(
    lastState: PayoutAccountOverviewUiState,
  ): PayoutAccountOverviewUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var uiState by remember { mutableStateOf<PayoutAccountOverviewUiState>(lastState) }

    LaunchedEffect(loadIteration) {
      uiState = PayoutAccountOverviewUiState.Loading
      getPayoutAccountUseCase.invoke().fold(
        ifLeft = { uiState = PayoutAccountOverviewUiState.Error },
        ifRight = { data ->
          uiState = if (data.currentMethod == null && data.availablePayoutMethods.isEmpty()) {
            PayoutAccountOverviewUiState.NoPayoutOptions
          } else {
            PayoutAccountOverviewUiState.Content(
              currentMethod = data.currentMethod,
              availablePayoutMethods = data.availablePayoutMethods,
            )
          }
        },
      )
    }

    CollectEvents { event ->
      when (event) {
        PayoutAccountOverviewEvent.Retry -> loadIteration++
      }
    }

    return uiState
  }
}
