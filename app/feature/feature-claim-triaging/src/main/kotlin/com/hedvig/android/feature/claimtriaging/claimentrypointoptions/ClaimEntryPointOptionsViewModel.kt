package com.hedvig.android.feature.claimtriaging.claimentrypointoptions

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimtriaging.EntryPointOption
import com.hedvig.android.feature.claimtriaging.ClaimTriagingDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ClaimEntryPointOptionsViewModel(
  private val savedStateHandle: SavedStateHandle,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {
  val claimEntryPointOptions = savedStateHandle.toRoute<ClaimTriagingDestination.ClaimEntryPointOptions>()

  private val _uiState = MutableStateFlow(ClaimEntryPointOptionsUiState(claimEntryPointOptions.entryPointOptions))
  val uiState = _uiState.asStateFlow()

  fun continueWithoutSelection() {
    _uiState.update { it.copy(haveTriedContinuingWithoutSelection = true) }
  }

  fun onSelectEntryPoint(entryPointOption: EntryPointOption) {
    _uiState.update {
      it.copy(
        selectedEntryPointOption = entryPointOption,
        haveTriedContinuingWithoutSelection = false,
      )
    }
  }

  fun startClaimFlow() {
    val uiState = _uiState.value
    val selectedEntryPointOption = uiState.selectedEntryPointOption ?: return
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      claimFlowRepository.startClaimFlow(claimEntryPointOptions.entryPointId, selectedEntryPointOption.id).fold(
        ifLeft = { errorMessage ->
          _uiState.update { it.copy(isLoading = false, startClaimErrorMessage = errorMessage.message) }
        },
        ifRight = { claimFlowStep ->
          _uiState.update { it.copy(isLoading = false, nextStep = claimFlowStep) }
        },
      )
    }
  }

  fun showedStartClaimError() {
    _uiState.update { it.copy(startClaimErrorMessage = null) }
  }

  fun handledNextStepNavigation() {
    _uiState.update { it.copy(nextStep = null) }
  }
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
