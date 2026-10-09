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
import com.hedvig.android.feature.payments.navigation.ManualChargeKey
import com.hedvig.android.feature.payments.navigation.ManualChargeSuccessKey
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class ManualChargeViewModel(
  getManualChargeInfoUseCase: GetManualChargeInfoUseCase,
  triggerManualCharge: TriggerManualChargeUseCase,
  backstack: Backstack,
) : MoleculeViewModel<ManualChargeEvent, ManualChargeUiState>(
    initialState = ManualChargeUiState.Loading,
    presenter = ManualChargePresenter(getManualChargeInfoUseCase, triggerManualCharge, backstack),
  )

private class ManualChargePresenter(
  private val getManualChargeInfoUseCase: GetManualChargeInfoUseCase,
  private val triggerManualCharge: TriggerManualChargeUseCase,
  private val backstack: Backstack,
) : MoleculePresenter<ManualChargeEvent, ManualChargeUiState> {
  @Composable
  override fun MoleculePresenterScope<ManualChargeEvent>.present(lastState: ManualChargeUiState): ManualChargeUiState {
    var dataLoadIteration by remember { mutableIntStateOf(0) }
    var refreshIteration by remember { mutableIntStateOf(0) }
    var screenState by remember { mutableStateOf(lastState) }
    var triggerChargeIteration by remember { mutableIntStateOf(0) }

    CollectEvents {
      when (it) {
        ManualChargeEvent.Retry -> {
          dataLoadIteration++
        }

        ManualChargeEvent.TriggerCharge -> {
          triggerChargeIteration++
        }

        ManualChargeEvent.Refresh -> {
          refreshIteration++
        }
      }
    }

    LaunchedEffect(triggerChargeIteration) {
      if (triggerChargeIteration > 0) {
        val currentState = screenState as? ManualChargeUiState.Success ?: return@LaunchedEffect
        screenState = currentState.copy(payButtonLoading = true)
        triggerManualCharge.invoke().fold(
          ifLeft = {
            screenState = ManualChargeUiState.Failure(it)
          },
          ifRight = {
            backstack.navigateAndPopUpTo<ManualChargeKey>(
              ManualChargeSuccessKey(
                showCancellationWarning = currentState.manualChargeInfo.showCancellationWarning,
              ),
              inclusive = true,
            )
          },
        )
      }
    }

    LaunchedEffect(dataLoadIteration) {
      screenState = ManualChargeUiState.Loading
      getManualChargeInfoUseCase.invoke().fold(
        ifRight = { result ->
          screenState = when (result) {
            is ManualChargeInfoResult.Chargeable -> ManualChargeUiState.Success(result.info)
            ManualChargeInfoResult.NoLongerChargeable -> ManualChargeUiState.NoLongerChargeable
          }
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
        screenState = when (result) {
          is ManualChargeInfoResult.Chargeable -> ManualChargeUiState.Success(
            manualChargeInfo = result.info,
            payButtonLoading = (screenState as? ManualChargeUiState.Success)?.payButtonLoading == true,
          )

          ManualChargeInfoResult.NoLongerChargeable -> ManualChargeUiState.NoLongerChargeable
        }
      }
    }
    return screenState
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
}

internal sealed interface ManualChargeEvent {
  data object Retry : ManualChargeEvent

  data object Refresh : ManualChargeEvent

  data object TriggerCharge : ManualChargeEvent
}
