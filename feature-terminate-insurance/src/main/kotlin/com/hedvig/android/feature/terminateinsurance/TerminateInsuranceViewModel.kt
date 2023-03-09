package com.hedvig.android.feature.terminateinsurance

import androidx.compose.material3.DatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceRepository
import com.hedvig.android.feature.terminateinsurance.data.TerminationStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

internal class TerminateInsuranceViewModel(
  private val insuranceId: InsuranceId,
  private val terminateInsuranceRepository: TerminateInsuranceRepository,
  clock: Clock = Clock.System,
) : ViewModel() {
  private val datePickerConfiguration = DatePickerConfiguration(clock)

  private val _uiState: MutableStateFlow<TerminateInsuranceUiState> = MutableStateFlow(
    TerminateInsuranceUiState(
      datePickerState = datePickerConfiguration.datePickerState,
      dateSubmissionError = false,
      dateSubmissionSuccess = false,
      isLoading = false,
      currentStep = null,
    ),
  )
  val uiState: StateFlow<TerminateInsuranceUiState> = _uiState.asStateFlow()

  init {
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      val step = terminateInsuranceRepository.startTerminationFlow(insuranceId)
      _uiState.update {
        it.copy(currentStep = step, isLoading = false)
      }
    }
  }

  fun handledSuccess() {
    _uiState.update {
      it.copy(dateSubmissionSuccess = false)
    }
  }

  fun showedError() {
    _uiState.update {
      it.copy(dateSubmissionError = false)
    }
  }

  fun submitSelectedDate() {
    val uiState = _uiState.value
    if (!uiState.canSubmitSelectedDate()) return
    val selectedDateMillis = uiState.datePickerState.selectedDateMillis ?: return
    val instant = Instant.fromEpochMilliseconds(selectedDateMillis)
    val selectedDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      val step = terminateInsuranceRepository.setTerminationDate(selectedDate)
      _uiState.update {
        it.copy(
          dateSubmissionError = false,
          isLoading = false,
          currentStep = step,
        )
      }
    }
  }
}

private class DatePickerConfiguration(clock: Clock) {
  private val todayAtStartOfDay: LocalDateTime = clock.now()
    .toLocalDateTime(TimeZone.UTC)
    .date
    .atStartOfDayIn(TimeZone.UTC)
    .toLocalDateTime(TimeZone.UTC)
  private val yearsRange = todayAtStartOfDay.year..2100

  val datePickerState = DatePickerState(null, null, yearsRange)
}

internal data class TerminateInsuranceUiState(
  val datePickerState: DatePickerState,
  val dateSubmissionError: Boolean,
  val dateSubmissionSuccess: Boolean,
  val isLoading: Boolean,
  val currentStep: TerminationStep?,
) {
  val canContinue: Boolean
    @Composable
    get() = remember(
      datePickerState.selectedDateMillis,
      dateSubmissionError,
      dateSubmissionSuccess,
      isLoading,
    ) { canSubmitSelectedDate() }
}

private fun TerminateInsuranceUiState.canSubmitSelectedDate(): Boolean {
  return datePickerState.selectedDateMillis != null &&
    !dateSubmissionError &&
    !dateSubmissionSuccess &&
    !isLoading
}
