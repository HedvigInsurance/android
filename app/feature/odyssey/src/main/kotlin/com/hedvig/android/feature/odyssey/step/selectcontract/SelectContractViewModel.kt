package com.hedvig.android.feature.odyssey.step.selectcontract

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.LocalContractContractOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SelectContractViewModel(
  initialSelectedContract: String?,
  private val contractOptions: List<LocalContractContractOption>,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {

  private val _uiState =
    MutableStateFlow(SelectContractUiState.fromInitialSelection(initialSelectedContract, contractOptions))
  val uiState: StateFlow<SelectContractUiState> = _uiState.asStateFlow()

  fun selectLocationOption(selectedContract: LocalContractContractOption) {
    _uiState.update { oldUiState ->
      val selectedValueExistsInOptions = selectedContract in contractOptions
      val contractIsAlreadySelected = oldUiState.selectedContract == selectedContract
      if (contractIsAlreadySelected || !selectedValueExistsInOptions) {
        oldUiState.copy(selectedContract = null)
      } else {
        oldUiState.copy(selectedContract = selectedContract)
      }
    }
  }

  fun showedError() {
    _uiState.update {
      it.copy(error = false)
    }
  }

  fun submitContract() {
    val uiState = _uiState.value
    val selectedContract = uiState.selectedContract
    if (selectedContract == null || !uiState.canSubmit) return
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      claimFlowRepository.submitContract(selectedContract.id).fold(
        ifLeft = {
          _uiState.update {
            it.copy(isLoading = false, error = true)
          }
        },
        ifRight = { claimFlowStep ->
          _uiState.update {
            it.copy(isLoading = false, nextStep = claimFlowStep)
          }
        },
      )
    }
  }

  fun handledNextStepNavigation() {
    _uiState.update { it.copy(nextStep = null) }
  }
}

internal data class SelectContractUiState(
  val contractOptions: List<LocalContractContractOption>,
  val selectedContract: LocalContractContractOption?,
  val isLoading: Boolean = false,
  val error: Boolean = false,
  val nextStep: ClaimFlowStep? = null,
) {
  val canSubmit: Boolean = selectedContract != null && !isLoading && !error && nextStep == null

  companion object {
    fun fromInitialSelection(
      initialSelectedLocation: String?,
      locationOptions: List<LocalContractContractOption>,
    ): SelectContractUiState {
      val selectedLocation = locationOptions
        .firstOrNull { it.id == initialSelectedLocation }
      return SelectContractUiState(
        locationOptions,
        selectedLocation,
      )
    }
  }
}
