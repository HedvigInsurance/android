package com.hedvig.android.feature.terminateinsurance.step.overview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.navigation.core.AppDestination
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
    viewModelScope.launch {
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
) {
  val canSubmit: Boolean
    @Composable
    get() = remember(
      nextStep,
      isLoading,
    ) { canSubmitSelectedDate() }
}

private fun OverviewUiState.canSubmitSelectedDate(): Boolean {
  return nextStep == null && !isLoading
}
