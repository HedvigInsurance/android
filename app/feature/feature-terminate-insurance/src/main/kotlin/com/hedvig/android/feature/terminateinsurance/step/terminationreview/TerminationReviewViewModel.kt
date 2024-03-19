package com.hedvig.android.feature.terminateinsurance.step.terminationreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.data.contract.ContractGroup
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
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class TerminationReviewViewModel(
  insuranceDisplayName: String,
  exposureName: String,
  contractGroup: ContractGroup,
  private val terminationType: TerminateInsuranceDestination.TerminationReview.TerminationType,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
  clock: Clock,
) : ViewModel() {
  private val _uiState: MutableStateFlow<OverviewUiState> = MutableStateFlow(
    OverviewUiState(
      terminationDate = terminationType
        .safeCast<TerminateInsuranceDestination.TerminationReview.TerminationType.Termination>()
        ?.terminationDate ?: clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
      insuranceDisplayName = insuranceDisplayName,
      exposureName = exposureName,
      contractGroup = contractGroup,
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
        TerminateInsuranceDestination.TerminationReview.TerminationType.Deletion -> {
          terminateInsuranceRepository.confirmDeletion()
        }

        is TerminateInsuranceDestination.TerminationReview.TerminationType.Termination -> {
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
  val terminationDate: LocalDate,
  val insuranceDisplayName: String,
  val nextStep: TerminateInsuranceStep?,
  val errorMessage: String?,
  val isSubmittingContractTermination: Boolean,
  val exposureName: String,
  val contractGroup: ContractGroup,
)
