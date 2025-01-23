package com.hedvig.android.feature.odyssey.step.selectcontract

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.LocalContractContractOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class SelectContractViewModel(
  selectContract: ClaimFlowDestination.SelectContract,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {
  private val _uiState = MutableStateFlow(SelectContractUiState.fromInitialSelection(selectContract.options))
  val uiState: StateFlow<SelectContractUiState> = _uiState.asStateFlow()

  fun selectContractOption(selectedContractId: String) {
    _uiState.update { oldUiState ->
      val selected = oldUiState.contractOptions.first { it.id == selectedContractId }
      oldUiState.copy(selectedContract = selected)
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
  val selectedContract: LocalContractContractOption,
  val isLoading: Boolean = false,
  val error: Boolean = false,
  val nextStep: ClaimFlowStep? = null,
) {
  val canSubmit: Boolean = !isLoading && !error && nextStep == null

  companion object {
    fun fromInitialSelection(locationOptions: List<LocalContractContractOption>): SelectContractUiState {
      return SelectContractUiState(
        contractOptions = locationOptions,
        selectedContract = locationOptions.first(),
      )
    }
  }
}
