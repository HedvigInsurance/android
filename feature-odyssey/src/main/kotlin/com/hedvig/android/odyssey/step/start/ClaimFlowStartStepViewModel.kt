package com.hedvig.android.odyssey.step.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.android.odyssey.data.ClaimFlowRepository
import com.hedvig.android.odyssey.data.ClaimFlowStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ClaimFlowStartStepViewModel(
  private val entryPointId: String?,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {
  private val _uiState: MutableStateFlow<ClaimFlowUiState> = MutableStateFlow(ClaimFlowUiState.Loading)
  private val retryChannel = RetryChannel()

  init {
    viewModelScope.launch {
      retryChannel.mapLatest {
        _uiState.update { ClaimFlowUiState.Loading }
        claimFlowRepository.startClaimFlow(entryPointId).fold(
          ifLeft = {
            _uiState.update { ClaimFlowUiState.Error }
          },
          ifRight = { terminationStep ->
            _uiState.update { ClaimFlowUiState.Success(terminationStep) }
          },
        )
      }.collect()
    }
  }

  val uiState: StateFlow<ClaimFlowUiState> = _uiState.asStateFlow()

  fun retryToStartClaimFlow() {
    retryChannel.retry()
  }

  fun handledNextStepNavigation() {
    val uiState = _uiState.value
    if (uiState is ClaimFlowUiState.Success) {
      _uiState.update { ClaimFlowUiState.Success(null) }
    }
  }
}

internal sealed interface ClaimFlowUiState {
  object Loading : ClaimFlowUiState
  object Error : ClaimFlowUiState
  data class Success(val nextStep: ClaimFlowStep?) : ClaimFlowUiState
}
