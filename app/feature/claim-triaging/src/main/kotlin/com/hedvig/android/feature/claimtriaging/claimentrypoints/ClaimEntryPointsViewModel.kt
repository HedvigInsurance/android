package com.hedvig.android.feature.claimtriaging.claimentrypoints

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimtriaging.EntryPoint
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ClaimEntryPointsViewModel(
  private val entryPoints: ImmutableList<EntryPoint>,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {
  private val _uiState = MutableStateFlow(ClaimEntryPointsUiState(entryPoints))
  val uiState = _uiState.asStateFlow()

  fun continueWithoutSelection() {
    _uiState.update { it.copy(haveTriedContinuingWithoutSelection = true) }
  }

  fun onSelectEntryPoint(entryPoint: EntryPoint) {
    _uiState.update {
      it.copy(
        selectedEntryPoint = entryPoint,
        haveTriedContinuingWithoutSelection = false,
      )
    }
  }

  fun startClaimFlow() {
    val uiState = _uiState.value
    if (uiState.isLoading) return
    val selectedEntryPoint = uiState.selectedEntryPoint ?: return
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      claimFlowRepository.startClaimFlow(selectedEntryPoint.id, null).fold(
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
internal data class ClaimEntryPointsUiState(
  val entryPoints: ImmutableList<EntryPoint>,
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
