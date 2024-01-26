package com.hedvig.android.feature.terminateinsurance.step.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.navigation.core.AppDestination
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

internal class OverviewViewModel(
  private val selectedDate: LocalDate,
  destination: AppDestination.TerminateInsurance,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
) : ViewModel() {
  private val _uiState: MutableStateFlow<OverviewUiState> = MutableStateFlow(
    OverviewUiState(
      selectedDate = selectedDate,
      insuranceDisplayName = destination.insuranceDisplayName,
      exposureName = destination.exposureName,
      contractGroup = destination.contractGroup,
      nextStep = null,
      errorMessage = null,
      isLoading = false,
    ),
  )
  val uiState: StateFlow<OverviewUiState> = _uiState.asStateFlow()

  fun submitSelectedDate() {
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      delay(3000) // Fake delay for better UX
      terminateInsuranceRepository.setTerminationDate(selectedDate).fold(
        ifLeft = { errorMessage ->
          _uiState.update {
            it.copy(
              errorMessage = errorMessage.message,
              isLoading = false,
            )
          }
        },
        ifRight = { terminateInsuranceStep: TerminateInsuranceStep ->
          _uiState.update {
            it.copy(
              nextStep = terminateInsuranceStep,
              isLoading = false,
            )
          }
        },
      )
    }
  }

  fun confirmDeletion() {
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      delay(3000) // Fake delay for better UX
      terminateInsuranceRepository.confirmDeletion().fold(
        ifLeft = { errorMessage ->
          _uiState.update {
            it.copy(
              isLoading = false,
              errorMessage = errorMessage.message,
            )
          }
        },
        ifRight = { terminateInsuranceFlowStep ->
          _uiState.update {
            it.copy(
              isLoading = false,
              nextStep = terminateInsuranceFlowStep,
            )
          }
        },
      )
    }
  }

  fun handledNextStepNavigation() {
    _uiState.update { it.copy(nextStep = null) }
  }
}

internal data class OverviewUiState(
  val selectedDate: LocalDate,
  val insuranceDisplayName: String,
  val nextStep: TerminateInsuranceStep?,
  val errorMessage: String?,
  val isLoading: Boolean,
  val exposureName: String,
  val contractGroup: ContractGroup,
)
