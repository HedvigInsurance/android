package com.hedvig.android.feature.terminateinsurance.step.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.navigation.core.AppDestination
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
      isSubmittingContractTermination = false,
    ),
  )
  val uiState: StateFlow<OverviewUiState> = _uiState.asStateFlow()

  fun terminateContractWithSelectedDate() {
    viewModelScope.launch {
      submitContractTermination { terminateInsuranceRepository.setTerminationDate(selectedDate) }
    }
  }

  fun submitContractDeletion() {
    viewModelScope.launch {
      submitContractTermination { terminateInsuranceRepository.confirmDeletion() }
    }
  }

  private suspend fun submitContractTermination(
    networkRequest: suspend () -> Either<ErrorMessage, TerminateInsuranceStep>,
  ) {
    _uiState.update { it.copy(isSubmittingContractTermination = true) }
    // Make the success response take at least 3 seconds as per the design
    coroutineScope {
      val minimumTimeDelay = async { delay(3000) }
      networkRequest().fold(
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
  val selectedDate: LocalDate,
  val insuranceDisplayName: String,
  val nextStep: TerminateInsuranceStep?,
  val errorMessage: String?,
  val isSubmittingContractTermination: Boolean,
  val exposureName: String,
  val contractGroup: ContractGroup,
)
