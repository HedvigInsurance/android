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
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class DeflectAutoDecomStepViewModel(
  terminateInsuranceRepository: TerminateInsuranceRepository,
) : MoleculeViewModel<DeflectAutoDecommissionEvent, DeflectAutoDecommissionUiState>(
    initialState = DeflectAutoDecommissionUiState.Success,
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
            currentState as? DeflectAutoDecommissionUiState.Loading ?: return@CollectEvents
          currentState = state.copy(nextStep = null)
        }

        DeflectAutoDecommissionEvent.FetchNextStep -> loadIteration++
      }
    }
    LaunchedEffect(loadIteration) {
      if (loadIteration > 0) {
        currentState = DeflectAutoDecommissionUiState.Loading(null)
        currentState = terminateInsuranceRepository.continueAfterAutoDecomDeflect().fold(
          ifLeft = {
            DeflectAutoDecommissionUiState.Failure
          },
          ifRight = { result ->
            DeflectAutoDecommissionUiState.Loading(
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
  data class Loading(
    val nextStep: TerminateInsuranceStep? = null,
  ) : DeflectAutoDecommissionUiState

  data object Success : DeflectAutoDecommissionUiState

  data object Failure : DeflectAutoDecommissionUiState
}

internal sealed interface DeflectAutoDecommissionEvent {
  data object FetchNextStep : DeflectAutoDecommissionEvent

  data object ClearTerminationStep : DeflectAutoDecommissionEvent
}
