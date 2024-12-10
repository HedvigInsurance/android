package com.hedvig.android.feature.terminateinsurance.step.terminationreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.feature.terminateinsurance.data.ExtraCoverageItem
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationGraphParameters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class TerminationConfirmationViewModel(
  private val terminationType: TerminateInsuranceDestination.TerminationConfirmation.TerminationType,
  private val insuranceInfo: TerminationGraphParameters,
  private val extraCoverageItems: List<ExtraCoverageItem>,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
) : ViewModel() {
  private val _uiState: MutableStateFlow<OverviewUiState> = MutableStateFlow(
    OverviewUiState(
      terminationType = terminationType,
      insuranceInfo = insuranceInfo,
      extraCoverageItems = extraCoverageItems,
      nextStep = null,
      errorMessage = null,
      isSubmittingContractTermination = false,
    ),
  )
  val uiState: StateFlow<OverviewUiState> = _uiState.asStateFlow()

  fun submitContractTermination() {
    _uiState.update { it.copy(isSubmittingContractTermination = true) }
    viewModelScope.launch {
      when (terminationType) {
        TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Deletion -> {
          terminateInsuranceRepository.confirmDeletion()
        }

        is TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Termination -> {
          terminateInsuranceRepository.setTerminationDate(terminationType.terminationDate)
        }
      }.fold(
        ifLeft = { errorMessage ->
          _uiState.update {
            it.copy(
              isSubmittingContractTermination = false,
              errorMessage = errorMessage.message,
            )
          }
        },
        ifRight = { terminateInsuranceFlowStep ->
          _uiState.update {
            it.copy(
              isSubmittingContractTermination = false,
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
  val terminationType: TerminateInsuranceDestination.TerminationConfirmation.TerminationType,
  val insuranceInfo: TerminationGraphParameters,
  val extraCoverageItems: List<ExtraCoverageItem>,
  val nextStep: TerminateInsuranceStep?,
  val errorMessage: String?,
  val isSubmittingContractTermination: Boolean,
)
