package com.hedvig.android.feature.terminateinsurance.step.deletion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class InsuranceDeletionViewModel(
  insuranceDeletion: TerminateInsuranceDestination.InsuranceDeletion,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
) : ViewModel() {

  private val _uiState: MutableStateFlow<InsuranceDeletionUiState> =
    MutableStateFlow(InsuranceDeletionUiState(insuranceDeletion.disclaimer))
  val uiState: StateFlow<InsuranceDeletionUiState> = _uiState.asStateFlow()

  fun confirmDeletion() {
    val uiState = _uiState.value
    if (!uiState.canSubmit) return
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      terminateInsuranceRepository.confirmDeletion().fold(
        ifLeft = {
          _uiState.update {
            uiState.copy(isLoading = false, hasError = true)
          }
        },
        ifRight = { terminateInsuranceFlowStep ->
          _uiState.update {
            uiState.copy(isLoading = false, nextStep = terminateInsuranceFlowStep)
          }
        },
      )
    }
  }

  fun handledNextStepNavigation() {
    _uiState.update { it.copy(nextStep = null) }
  }

  fun showedError() {
    _uiState.update { it.copy(hasError = false) }
  }
}

internal data class InsuranceDeletionUiState(
  val disclaimer: String,
  val hasError: Boolean = false,
  val isLoading: Boolean = false,
  val nextStep: TerminateInsuranceStep? = null,
) {
  val canSubmit: Boolean
    get() = !hasError && !isLoading && nextStep == null
}
