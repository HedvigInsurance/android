package com.hedvig.android.feature.terminateinsurance.step.deflectAutoDecom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel

internal class DeflectAutoDecommissionStepViewModel(
  terminateInsuranceRepository: TerminateInsuranceRepository,
) : MoleculeViewModel<DeflectAutoDecommissionEvent, DeflectAutoDecommissionUiState>(
    initialState = DeflectAutoDecommissionUiState.Success(),
    presenter = DeflectAutoDecomStepPresenter(terminateInsuranceRepository),
  )

private class DeflectAutoDecomStepPresenter(
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
) : MoleculePresenter<DeflectAutoDecommissionEvent, DeflectAutoDecommissionUiState> {
  @Composable
  override fun MoleculePresenterScope<DeflectAutoDecommissionEvent>.present(
    lastState: DeflectAutoDecommissionUiState,
  ): DeflectAutoDecommissionUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var currentState by remember {
      mutableStateOf(lastState)
    }
    CollectEvents { event ->
      when (event) {
        DeflectAutoDecommissionEvent.ClearTerminationStep -> {
          val state =
            currentState as? DeflectAutoDecommissionUiState.Success ?: return@CollectEvents
          currentState = state.copy(nextStep = null, buttonLoading = false)
        }

        DeflectAutoDecommissionEvent.FetchNextStep -> loadIteration++
      }
    }
    LaunchedEffect(loadIteration) {
      if (loadIteration > 0) {
        val state = currentState
        currentState = when (state) {
          DeflectAutoDecommissionUiState.Failure -> DeflectAutoDecommissionUiState.Loading
          DeflectAutoDecommissionUiState.Loading -> return@LaunchedEffect
          is DeflectAutoDecommissionUiState.Success -> state.copy(buttonLoading = true)
        }
        currentState = terminateInsuranceRepository.continueAfterAutoDecomDeflect().fold(
          ifLeft = {
            DeflectAutoDecommissionUiState.Failure
          },
          ifRight = { result ->
            DeflectAutoDecommissionUiState.Success(
              buttonLoading = true,
              nextStep = result,
            )
          },
        )
      }
    }
    return currentState
  }
}

internal sealed interface DeflectAutoDecommissionUiState {
  data object Loading : DeflectAutoDecommissionUiState

  data class Success(
    val buttonLoading: Boolean = false,
    val nextStep: TerminateInsuranceStep? = null,
  ) : DeflectAutoDecommissionUiState

  data object Failure : DeflectAutoDecommissionUiState
}

internal sealed interface DeflectAutoDecommissionEvent {
  data object FetchNextStep : DeflectAutoDecommissionEvent

  data object ClearTerminationStep : DeflectAutoDecommissionEvent
}
