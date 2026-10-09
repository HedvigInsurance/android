package com.hedvig.android.feature.payoutaccount.ui.overview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.feature.payoutaccount.data.GetPayoutAccountUseCase
import com.hedvig.android.feature.payoutaccount.data.PayoutAccount
import com.hedvig.android.feature.payoutaccount.data.PayoutAccountData
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class PayoutAccountOverviewViewModel(
  getPayoutAccountUseCase: GetPayoutAccountUseCase,
) : MoleculeViewModel<PayoutAccountOverviewEvent, PayoutAccountOverviewUiState>(
    PayoutAccountOverviewUiState.Loading,
    PayoutAccountOverviewPresenter(getPayoutAccountUseCase),
  )

internal sealed interface PayoutAccountOverviewEvent {
  data object Retry : PayoutAccountOverviewEvent

  data object Refresh : PayoutAccountOverviewEvent
}

internal sealed interface PayoutAccountOverviewUiState {
  data object Loading : PayoutAccountOverviewUiState

  data object Error : PayoutAccountOverviewUiState

  data object NoPayoutOptions : PayoutAccountOverviewUiState

  data class Content(
    val currentMethod: PayoutAccount?,
    val availablePayoutMethods: List<PaymentProvider>,
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
    var refreshIteration by remember { mutableIntStateOf(0) }
    var uiState by remember { mutableStateOf<PayoutAccountOverviewUiState>(lastState) }

    LaunchedEffect(loadIteration) {
      uiState = PayoutAccountOverviewUiState.Loading
      getPayoutAccountUseCase.invoke().fold(
        ifLeft = { uiState = PayoutAccountOverviewUiState.Error },
        ifRight = { data -> uiState = data.toUiState() },
      )
    }

    // Picks up a method connected further down the flow, which lands back here on a screen that was
    // never torn down. It refreshes in place and keeps what is on screen if the refetch fails, so
    // returning here never flashes to loading or to an error over a method that is still valid.
    LaunchedEffect(refreshIteration) {
      if (refreshIteration == 0) return@LaunchedEffect
      getPayoutAccountUseCase.invoke().onRight { data -> uiState = data.toUiState() }
    }

    CollectEvents { event ->
      when (event) {
        PayoutAccountOverviewEvent.Retry -> loadIteration++
        PayoutAccountOverviewEvent.Refresh -> refreshIteration++
      }
    }

    return uiState
  }
}

private fun PayoutAccountData.toUiState(): PayoutAccountOverviewUiState {
  return if (currentMethod == null && availablePayoutMethods.isEmpty()) {
    PayoutAccountOverviewUiState.NoPayoutOptions
  } else {
    PayoutAccountOverviewUiState.Content(
      currentMethod = currentMethod,
      availablePayoutMethods = availablePayoutMethods,
    )
  }
}
