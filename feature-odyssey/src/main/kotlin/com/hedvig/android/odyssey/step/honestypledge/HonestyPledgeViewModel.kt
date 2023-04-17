package com.hedvig.android.odyssey.step.honestypledge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.odyssey.data.ClaimFlowRepository
import com.hedvig.android.odyssey.data.ClaimFlowStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class HonestyPledgeViewModel(
  private val entryPointId: String?,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {
  private val _uiState: MutableStateFlow<HonestyPledgeUiState> = MutableStateFlow(HonestyPledgeUiState())
  val uiState: StateFlow<HonestyPledgeUiState> = _uiState.asStateFlow()

  fun startClaimFlow() {
    if (_uiState.value.isLoading) return
    _uiState.update { it.copy(isLoading = true, hasError = false) }
    viewModelScope.launch {
      claimFlowRepository.startClaimFlow(entryPointId).fold(
        ifLeft = {
          _uiState.update { it.copy(isLoading = false, hasError = true) }
        },
        ifRight = { claimFlowStep ->
          _uiState.update { it.copy(isLoading = false, nextStep = claimFlowStep) }
        },
      )
    }
  }

  fun handledNextStepNavigation() {
    _uiState.update { it.copy(nextStep = null) }
  }
}

internal data class HonestyPledgeUiState(
  val nextStep: ClaimFlowStep? = null,
  val isLoading: Boolean = false,
  val hasError: Boolean = false,
)
