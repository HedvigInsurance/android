package com.hedvig.android.feature.payments.ui.manualcharge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.payments.data.GetManualChargeInfoUseCase
import com.hedvig.android.feature.payments.data.ManualChargeInfo
import com.hedvig.android.feature.payments.data.ManualChargeInfoResult
import com.hedvig.android.feature.payments.data.TriggerManualChargeUseCase
import com.hedvig.android.molecule.public.AttemptStatus
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.molecule.public.NavigationRequest
import com.hedvig.android.molecule.public.rememberAttempts
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class ManualChargeViewModel(
  getManualChargeInfoUseCase: GetManualChargeInfoUseCase,
  triggerManualCharge: TriggerManualChargeUseCase,
) : MoleculeViewModel<ManualChargeEvent, ManualChargeUiState>(
    initialState = ManualChargeUiState.Loading,
    presenter = ManualChargePresenter(getManualChargeInfoUseCase, triggerManualCharge),
  )

internal class ManualChargePresenter(
  private val getManualChargeInfoUseCase: GetManualChargeInfoUseCase,
  private val triggerManualCharge: TriggerManualChargeUseCase,
) : MoleculePresenter<ManualChargeEvent, ManualChargeUiState> {
  @Composable
  override fun MoleculePresenterScope<ManualChargeEvent>.present(lastState: ManualChargeUiState): ManualChargeUiState {
    var dataLoadIteration by remember { mutableIntStateOf(0) }
    var refreshIteration by remember { mutableIntStateOf(0) }
    var screenState by remember { mutableStateOf(lastState) }
    // Details shown before this run started may be stale, so only details loaded in this run can be charged.
    var loadedInThisRun by remember { mutableStateOf(false) }
    val charge = rememberAttempts<Unit>(
      restoredWhileRunning = (lastState as? ManualChargeUiState.Success)?.payButtonLoading == true,
      terminal = true,
    ) {
      val currentState = screenState as? ManualChargeUiState.Success ?: return@rememberAttempts false
      triggerManualCharge.invoke().fold(
        ifLeft = {
          screenState = ManualChargeUiState.Failure(it)
          false
        },
        ifRight = {
          screenState = ManualChargeUiState.Charged(currentState.manualChargeInfo)
          true
        },
      )
    }

    CollectEvents {
      when (it) {
        ManualChargeEvent.Retry -> {
          dataLoadIteration++
        }

        ManualChargeEvent.TriggerCharge -> {
          val current = screenState
          if (loadedInThisRun && current is ManualChargeUiState.Success) {
            charge.start(Unit)
          }
        }

        ManualChargeEvent.Refresh -> {
          refreshIteration++
        }
      }
    }

    LaunchedEffect(dataLoadIteration) {
      // The member has paid, so nothing is loaded again, also when the presenter restarts from this state.
      if (screenState is ManualChargeUiState.Charged) return@LaunchedEffect
      screenState = ManualChargeUiState.Loading
      getManualChargeInfoUseCase.invoke().fold(
        ifRight = { result ->
          screenState = when (result) {
            is ManualChargeInfoResult.Chargeable -> ManualChargeUiState.Success(result.info)
            ManualChargeInfoResult.NoLongerChargeable -> ManualChargeUiState.NoLongerChargeable
          }
          loadedInThisRun = true
        },
        ifLeft = { failure ->
          screenState = ManualChargeUiState.Failure(failure)
        },
      )
    }

    // Picks up a payin method connected or made primary further down the flow. Only a failure to
    // reach the backend is ignored, so coming back never flashes to loading or to an error over
    // details the member can still act on. The backend withdrawing the charge is not ignored: those
    // details are no longer payable, so leaving them on screen would offer a charge that cannot go
    // through.
    LaunchedEffect(refreshIteration) {
      if (refreshIteration == 0) return@LaunchedEffect
      getManualChargeInfoUseCase.invoke().onRight { result ->
        if (screenState is ManualChargeUiState.Charged) return@onRight
        screenState = when (result) {
          is ManualChargeInfoResult.Chargeable -> ManualChargeUiState.Success(result.info)
          ManualChargeInfoResult.NoLongerChargeable -> ManualChargeUiState.NoLongerChargeable
        }
      }
    }
    val state = screenState
    if (state !is ManualChargeUiState.Success) return state
    return when (charge.status) {
      AttemptStatus.Running -> state.copy(payButtonLoading = true)

      // The charge stopped with the last presenter run and may have gone through. The load effect fetches the details
      // again on every restart, and the backend's answer replaces this state.
      AttemptStatus.Interrupted -> state.copy(payButtonLoading = false)

      AttemptStatus.Idle -> state.copy(payButtonLoading = false)
    }
  }
}

internal sealed interface ManualChargeUiState {
  data object Loading : ManualChargeUiState

  /** Nothing left for the member to settle here, so the screen offers a way to ask about it instead. */
  data object NoLongerChargeable : ManualChargeUiState

  data class Failure(
    val error: ErrorMessage,
  ) : ManualChargeUiState

  data class Success(
    val manualChargeInfo: ManualChargeInfo,
    val payButtonLoading: Boolean = false,
  ) : ManualChargeUiState

  /** The charge went through. The platform leaves the screen; nothing follows this state. */
  data class Charged(
    val manualChargeInfo: ManualChargeInfo,
  ) : ManualChargeUiState {
    /**
     * Leaving for the success screen. It is not part of equality, so the state flow keeps the first of equal states,
     * and a restart that produces this state again does not offer a second request.
     */
    val leave: NavigationRequest<ManualChargeInfo> = NavigationRequest.of(manualChargeInfo)
  }
}

internal sealed interface ManualChargeEvent {
  data object Retry : ManualChargeEvent

  data object Refresh : ManualChargeEvent

  data object TriggerCharge : ManualChargeEvent
}
