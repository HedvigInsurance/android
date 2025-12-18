package com.hedvig.android.feature.claimtriaging.claimentrypoints

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimtriaging.EntryPoint
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlinx.coroutines.launch

internal class ClaimEntryPointsViewModel(
  entryPoints: List<EntryPoint>,
  claimFlowRepository: ClaimFlowRepository,
) : MoleculeViewModel<ClaimEntryPointsEvent, ClaimEntryPointsUiState>(
    initialState = ClaimEntryPointsUiState(entryPoints),
    presenter = ClaimEntryPointsPresenter(claimFlowRepository),
  )

internal class ClaimEntryPointsPresenter(
  private val claimFlowRepository: ClaimFlowRepository,
) : MoleculePresenter<ClaimEntryPointsEvent, ClaimEntryPointsUiState> {
  @Composable
  override fun MoleculePresenterScope<ClaimEntryPointsEvent>.present(
    lastState: ClaimEntryPointsUiState,
  ): ClaimEntryPointsUiState {
    var uiState by remember { mutableStateOf(lastState) }

    CollectEvents { event ->
      when (event) {
        ClaimEntryPointsEvent.ContinueWithoutSelection -> {
          uiState = uiState.copy(haveTriedContinuingWithoutSelection = true)
        }

        is ClaimEntryPointsEvent.SelectEntryPoint -> {
          uiState = uiState.copy(
            selectedEntryPoint = event.entryPoint,
            haveTriedContinuingWithoutSelection = false,
          )
        }

        ClaimEntryPointsEvent.StartClaimFlow -> {
          if (uiState.isLoading) return@CollectEvents
          val selectedEntryPoint = uiState.selectedEntryPoint ?: return@CollectEvents
          uiState = uiState.copy(isLoading = true)
          launch {
            claimFlowRepository.startClaimFlow(selectedEntryPoint.id, null).fold(
              ifLeft = { errorMessage ->
                uiState = uiState.copy(
                  isLoading = false,
                  startClaimErrorMessage = errorMessage.message,
                )
              },
              ifRight = { claimFlowStep ->
                uiState = uiState.copy(
                  isLoading = false,
                  nextStep = claimFlowStep,
                )
              },
            )
          }
        }

        ClaimEntryPointsEvent.DismissStartClaimError -> {
          uiState = uiState.copy(startClaimErrorMessage = null)
        }

        ClaimEntryPointsEvent.HandledNextStepNavigation -> {
          uiState = uiState.copy(nextStep = null)
        }
      }
    }

    return uiState
  }
}

internal sealed interface ClaimEntryPointsEvent {
  data object ContinueWithoutSelection : ClaimEntryPointsEvent

  data class SelectEntryPoint(val entryPoint: EntryPoint) : ClaimEntryPointsEvent

  data object StartClaimFlow : ClaimEntryPointsEvent

  data object DismissStartClaimError : ClaimEntryPointsEvent

  data object HandledNextStepNavigation : ClaimEntryPointsEvent
}

@Immutable
internal data class ClaimEntryPointsUiState(
  val entryPoints: List<EntryPoint>,
  val selectedEntryPoint: EntryPoint? = null,
  val haveTriedContinuingWithoutSelection: Boolean = false,
  val isLoading: Boolean = false,
  val startClaimErrorMessage: String? = null,
  val nextStep: ClaimFlowStep? = null,
) {
  val canContinue: Boolean
    get() = isLoading == false &&
      startClaimErrorMessage == null &&
      nextStep == null
}
