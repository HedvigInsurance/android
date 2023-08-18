package com.hedvig.android.feature.terminateinsurance.step.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.android.feature.terminateinsurance.InsuranceId
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class TerminationStartStepViewModel(
  private val insuranceId: InsuranceId,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
) : ViewModel() {
  private val _uiState: MutableStateFlow<TerminationFlowUiState> =
    MutableStateFlow(TerminationFlowUiState.Loading)
  private val retryChannel = RetryChannel()

  init {
    viewModelScope.launch {
      retryChannel.mapLatest {
        _uiState.update { TerminationFlowUiState.Loading }
        terminateInsuranceRepository.startTerminationFlow(insuranceId).fold(
          ifLeft = {
            _uiState.update { TerminationFlowUiState.Error }
          },
          ifRight = { terminationStep ->
            _uiState.update { TerminationFlowUiState.Success(terminationStep) }
          },
        )
      }.collect()
    }
  }

  val uiState: StateFlow<TerminationFlowUiState> = _uiState.asStateFlow()

  fun retryToStartTerminationFlow() {
    retryChannel.retry()
  }

  fun handledNextStepNavigation() {
    val uiState = _uiState.value
    if (uiState is TerminationFlowUiState.Success) {
      _uiState.update { TerminationFlowUiState.Success(null) }
    }
  }
}

internal sealed interface TerminationFlowUiState {
  data object Loading : TerminationFlowUiState
  data object Error : TerminationFlowUiState
  data class Success(val nextStep: TerminateInsuranceStep?) : TerminationFlowUiState
}
