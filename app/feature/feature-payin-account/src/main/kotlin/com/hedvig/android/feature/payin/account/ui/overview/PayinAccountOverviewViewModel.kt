package com.hedvig.android.feature.payin.account.ui.overview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.payin.account.data.GetPayinAccountUseCase
import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject
import octopus.type.MemberPaymentProvider

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class PayinAccountOverviewViewModel(
  getPayinAccountUseCase: GetPayinAccountUseCase,
) : MoleculeViewModel<PayinAccountOverviewEvent, PayinAccountOverviewUiState>(
    PayinAccountOverviewUiState.Loading,
    PayinAccountOverviewPresenter(getPayinAccountUseCase),
  )

internal sealed interface PayinAccountOverviewEvent {
  data object Retry : PayinAccountOverviewEvent

  data object Refresh : PayinAccountOverviewEvent
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
    var refreshIteration by remember { mutableIntStateOf(0) }
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

    // Picks up methods connected or removed further down the flow. It refreshes in place and keeps
    // what is on screen if the refetch fails, so returning here never flashes to loading or to an
    // error over a list that is still perfectly usable.
    LaunchedEffect(refreshIteration) {
      if (refreshIteration == 0) return@LaunchedEffect
      getPayinAccountUseCase.invoke().onRight { data ->
        uiState = PayinAccountOverviewUiState.Content(
          currentMethods = data.currentMethods,
          availablePayinMethods = data.availablePayinMethods,
        )
      }
    }

    CollectEvents { event ->
      when (event) {
        PayinAccountOverviewEvent.Retry -> loadIteration++
        PayinAccountOverviewEvent.Refresh -> refreshIteration++
      }
    }

    return uiState
  }
}
