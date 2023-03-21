package com.hedvig.android.odyssey.step.dateofoccurrence

import androidx.compose.material3.DatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.odyssey.data.ClaimFlowRepository
import com.hedvig.android.odyssey.data.ClaimFlowStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

internal class DateOfOccurrenceViewModel(
  initialDateOfOccurrence: LocalDate?,
  maxDate: LocalDate,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {
  private val datePickerConfiguration = DatePickerConfiguration(initialDateOfOccurrence, maxDate)
  val dateValidator = datePickerConfiguration.dateValidator

  private val _uiState: MutableStateFlow<DateOfOccurrenceUiState> = MutableStateFlow(
    DateOfOccurrenceUiState(
      datePickerState = datePickerConfiguration.datePickerState,
      dateSubmissionError = false,
      nextStep = null,
      isLoading = false,
    ),
  )
  val uiState: StateFlow<DateOfOccurrenceUiState> = _uiState.asStateFlow()

  fun submitSelectedDate() {
    val uiState = _uiState.value
    if (!uiState.canSubmitSelectedDate()) return
    val selectedDateMillis = uiState.datePickerState.selectedDateMillis ?: return
    _uiState.update { it.copy(isLoading = true) }
    val selectedDate = Instant.fromEpochMilliseconds(selectedDateMillis).toLocalDateTime(TimeZone.UTC).date
    viewModelScope.launch {
      claimFlowRepository.submitDateOfOccurrence(selectedDate).fold(
        ifLeft = {
          _uiState.update {
            it.copy(
              dateSubmissionError = true,
              isLoading = false,
            )
          }
        },
        ifRight = { claimFlowStep: ClaimFlowStep ->
          _uiState.update {
            it.copy(
              nextStep = claimFlowStep,
              isLoading = false,
            )
          }
        },
      )
    }
  }

  fun handledNextStepNavigation() {
    _uiState.update {
      it.copy(nextStep = null)
    }
  }

  fun showedError() {
    _uiState.update {
      it.copy(dateSubmissionError = false)
    }
  }
}

private class DatePickerConfiguration(dateOfOccurrence: LocalDate?, maxDate: LocalDate) {
  private val minDate = LocalDate(1900, 1, 1)
  private val minDateInMillis = minDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  private val maxDateInMillis = maxDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  private val yearsRange = minDate.year..maxDate.year

  val datePickerState = DatePickerState(
    initialSelectedDateMillis = dateOfOccurrence?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds(),
    initialDisplayedMonthMillis = null,
    yearsRange = yearsRange,
  )
  val dateValidator: (Long) -> Boolean = { selectedDateEpochMillis ->
    selectedDateEpochMillis in minDateInMillis..maxDateInMillis
  }
}

internal data class DateOfOccurrenceUiState(
  val datePickerState: DatePickerState,
  val dateSubmissionError: Boolean,
  val nextStep: ClaimFlowStep?,
  val isLoading: Boolean,
) {
  val canContinue: Boolean
    @Composable
    get() = remember(
      datePickerState.selectedDateMillis,
      dateSubmissionError,
      nextStep,
      isLoading,
    ) { canSubmitSelectedDate() }
}

private fun DateOfOccurrenceUiState.canSubmitSelectedDate(): Boolean {
  return datePickerState.selectedDateMillis != null &&
    !dateSubmissionError &&
    nextStep == null &&
    !isLoading
}
