package com.hedvig.android.feature.odyssey.step.informdeflect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.EmergencyOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ConfirmEmergencyViewModel(
  confirmEmergency: ClaimFlowDestination.ConfirmEmergency,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {
  private val _uiState = MutableStateFlow(
    ConfirmEmergencyUiState(
      title = confirmEmergency.text,
      options = confirmEmergency.options,
      selectedOption = null,
      isLoading = false,
      error = false,
      nextStep = null,
    ),
  )
  val uiState: StateFlow<ConfirmEmergencyUiState> = _uiState.asStateFlow()

  fun submitIsUrgentEmergency() {
    val selectedOption = uiState.value.selectedOption
    if (selectedOption == null) {
      _uiState.update { it.copy(haveTriedContinuingWithoutSelection = true) }
    } else {
      viewModelScope.launch {
        _uiState.update { it.copy(isLoading = false) }
        claimFlowRepository.submitUrgentEmergency(selectedOption.value).fold(
          ifLeft = {
            _uiState.update {
              it.copy(
                error = true,
                isLoading = false,
              )
            }
          },
          ifRight = { nextStep ->
            _uiState.update {
              it.copy(
                nextStep = nextStep,
                isLoading = false,
              )
            }
          },
        )
      }
    }
  }

  fun selectOption(emergencyOption: EmergencyOption) {
    _uiState.update {
      it.copy(
        selectedOption = emergencyOption,
        haveTriedContinuingWithoutSelection = false,
      )
    }
  }

  fun handledNextStepNavigation() {
    _uiState.update { it.copy(nextStep = null) }
  }
}

internal data class ConfirmEmergencyUiState(
  val title: String,
  val options: List<EmergencyOption>,
  val selectedOption: EmergencyOption?,
  val haveTriedContinuingWithoutSelection: Boolean = false,
  val isLoading: Boolean = false,
  val error: Boolean = false,
  val nextStep: ClaimFlowStep? = null,
) {
  val canSubmit: Boolean = !error && nextStep == null
}
