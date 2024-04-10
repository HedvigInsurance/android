package com.hedvig.android.feature.terminateinsurance.step.terminationreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.feature.terminateinsurance.navigation.TerminateInsuranceDestination
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class TerminationConfirmationViewModel(
  private val terminationType: TerminateInsuranceDestination.TerminationConfirmation.TerminationType,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
) : ViewModel() {
  private val _uiState: MutableStateFlow<OverviewUiState> = MutableStateFlow(
    OverviewUiState(
      terminationType = terminationType,
      nextStep = null,
      errorMessage = null,
      isSubmittingContractTermination = false,
    ),
  )
  val uiState: StateFlow<OverviewUiState> = _uiState.asStateFlow()

  fun submitContractTermination() {
    _uiState.update { it.copy(isSubmittingContractTermination = true) }
    viewModelScope.launch {
      // Make the success response take at least 3 seconds as per the design
      val minimumTimeDelay = async { delay(3000) }
      when (terminationType) {
        TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Deletion -> {
          terminateInsuranceRepository.confirmDeletion()
        }

        is TerminateInsuranceDestination.TerminationConfirmation.TerminationType.Termination -> {
          terminateInsuranceRepository.setTerminationDate(terminationType.terminationDate)
        }
      }.fold(
        ifLeft = { errorMessage ->
          minimumTimeDelay.cancel()
          _uiState.update {
            it.copy(
              isSubmittingContractTermination = false,
              errorMessage = errorMessage.message,
            )
          }
        },
        ifRight = { terminateInsuranceFlowStep ->
          minimumTimeDelay.await()
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
  val nextStep: TerminateInsuranceStep?,
  val errorMessage: String?,
  val isSubmittingContractTermination: Boolean,
)
