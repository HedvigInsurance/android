package com.hedvig.android.feature.claimtriaging.claimentrypointoptions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimtriaging.EntryPointId
import com.hedvig.android.data.claimtriaging.EntryPointOption
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlinx.coroutines.launch

internal class ClaimEntryPointOptionsViewModel(
  entryPointId: EntryPointId,
  entryPointOptions: List<EntryPointOption>,
  claimFlowRepository: ClaimFlowRepository,
) : MoleculeViewModel<ClaimEntryPointOptionsEvent, ClaimEntryPointOptionsUiState>(
    initialState = ClaimEntryPointOptionsUiState(entryPointOptions),
    presenter = ClaimEntryPointOptionsPresenter(entryPointId, entryPointOptions, claimFlowRepository),
  )

internal class ClaimEntryPointOptionsPresenter(
  private val entryPointId: EntryPointId,
  private val entryPointOptions: List<EntryPointOption>,
  private val claimFlowRepository: ClaimFlowRepository,
) : MoleculePresenter<ClaimEntryPointOptionsEvent, ClaimEntryPointOptionsUiState> {
  @Composable
  override fun MoleculePresenterScope<ClaimEntryPointOptionsEvent>.present(
    lastState: ClaimEntryPointOptionsUiState,
  ): ClaimEntryPointOptionsUiState {
    var uiState by remember { mutableStateOf(lastState) }

    CollectEvents { event ->
      when (event) {
        ClaimEntryPointOptionsEvent.ContinueWithoutSelection -> {
          uiState = uiState.copy(haveTriedContinuingWithoutSelection = true)
        }

        is ClaimEntryPointOptionsEvent.SelectEntryPointOption -> {
          uiState = uiState.copy(
            selectedEntryPointOption = event.entryPointOption,
            haveTriedContinuingWithoutSelection = false,
          )
        }

        ClaimEntryPointOptionsEvent.StartClaimFlow -> {
          if (uiState.isLoading) return@CollectEvents
          val selectedEntryPointOption = uiState.selectedEntryPointOption ?: return@CollectEvents
          uiState = uiState.copy(isLoading = true)
          launch {
            claimFlowRepository.startClaimFlow(entryPointId, selectedEntryPointOption.id).fold(
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

        ClaimEntryPointOptionsEvent.DismissStartClaimError -> {
          uiState = uiState.copy(startClaimErrorMessage = null)
        }

        ClaimEntryPointOptionsEvent.HandledNextStepNavigation -> {
          uiState = uiState.copy(nextStep = null)
        }
      }
    }

    return uiState
  }
}

internal sealed interface ClaimEntryPointOptionsEvent {
  data object ContinueWithoutSelection : ClaimEntryPointOptionsEvent

  data class SelectEntryPointOption(val entryPointOption: EntryPointOption) : ClaimEntryPointOptionsEvent

  data object StartClaimFlow : ClaimEntryPointOptionsEvent

  data object DismissStartClaimError : ClaimEntryPointOptionsEvent

  data object HandledNextStepNavigation : ClaimEntryPointOptionsEvent
}

@Immutable
internal data class ClaimEntryPointOptionsUiState(
  val entryPointOptions: List<EntryPointOption>,
  val selectedEntryPointOption: EntryPointOption? = null,
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
