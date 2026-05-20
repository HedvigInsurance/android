package com.hedvig.android.feature.payin.account.ui.overview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.payin.account.data.GetPayinAccountUseCase
import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import octopus.type.MemberPaymentProvider

internal class PayinAccountOverviewViewModel(
  getPayinAccountUseCase: GetPayinAccountUseCase,
) : MoleculeViewModel<PayinAccountOverviewEvent, PayinAccountOverviewUiState>(
  PayinAccountOverviewUiState.Loading,
  PayinAccountOverviewPresenter(getPayinAccountUseCase),
)

internal sealed interface PayinAccountOverviewEvent {
  data object Retry : PayinAccountOverviewEvent
}

internal sealed interface PayinAccountOverviewUiState {
  data object Loading : PayinAccountOverviewUiState

  data object Error : PayinAccountOverviewUiState

  data class Content(
    val currentMethods: List<PayinAccount>,
    val availablePayinMethods: List<MemberPaymentProvider>,
  ) : PayinAccountOverviewUiState
}

internal class PayinAccountOverviewPresenter(
  private val getPayinAccountUseCase: GetPayinAccountUseCase,
) : MoleculePresenter<PayinAccountOverviewEvent, PayinAccountOverviewUiState> {
  @Composable
  override fun MoleculePresenterScope<PayinAccountOverviewEvent>.present(
    lastState: PayinAccountOverviewUiState,
  ): PayinAccountOverviewUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var uiState by remember { mutableStateOf<PayinAccountOverviewUiState>(lastState) }

    LaunchedEffect(loadIteration) {
      uiState = PayinAccountOverviewUiState.Loading
      getPayinAccountUseCase.invoke().fold(
        ifLeft = { uiState = PayinAccountOverviewUiState.Error },
        ifRight = { data ->
          uiState = PayinAccountOverviewUiState.Content(
              currentMethods = data.currentMethods,
              availablePayinMethods = data.availablePayinMethods,
            )

        },
      )
    }

    CollectEvents { event ->
      when (event) {
        PayinAccountOverviewEvent.Retry -> loadIteration++
      }
    }

    return uiState
  }
}
