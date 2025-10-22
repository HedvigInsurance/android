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
) : MoleculeViewModel<DeflectAutoDecomEvent, DeflectAutoDecomUiState>(
    initialState = DeflectAutoDecomUiState.Success,
    presenter = DeflectAutoDecomStepPresenter(terminateInsuranceRepository),
  )

private class DeflectAutoDecomStepPresenter(
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
) : MoleculePresenter<DeflectAutoDecomEvent, DeflectAutoDecomUiState> {
  @Composable
  override fun MoleculePresenterScope<DeflectAutoDecomEvent>.present(
    lastState: DeflectAutoDecomUiState,
  ): DeflectAutoDecomUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var currentState by remember {
      mutableStateOf(lastState)
    }
    CollectEvents { event ->
      when (event) {
        DeflectAutoDecomEvent.ClearTerminationStep -> {
          val state =
            currentState as? DeflectAutoDecomUiState.Loading ?: return@CollectEvents
          currentState = state.copy(nextStep = null)
        }

        DeflectAutoDecomEvent.FetchNextStep -> loadIteration++
      }
    }
    LaunchedEffect(loadIteration) {
      if (loadIteration > 0) {
        currentState = DeflectAutoDecomUiState.Loading(null)
        currentState = terminateInsuranceRepository.continueAfterAutoDecomDeflect().fold(
          ifLeft = {
            DeflectAutoDecomUiState.Failure
          },
          ifRight = { result ->
            DeflectAutoDecomUiState.Loading(
              nextStep = result,
            )
          },
        )
      }
    }
    return currentState
  }
}

internal sealed interface DeflectAutoDecomUiState {
  data class Loading(
    val nextStep: TerminateInsuranceStep? = null,
  ) : DeflectAutoDecomUiState

  data object Success : DeflectAutoDecomUiState

  data object Failure : DeflectAutoDecomUiState
}

internal sealed interface DeflectAutoDecomEvent {
  data object FetchNextStep : DeflectAutoDecomEvent

  data object ClearTerminationStep : DeflectAutoDecomEvent
}
