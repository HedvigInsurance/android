package com.hedvig.android.feature.terminateinsurance.step.terminationdate

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

internal class TerminationDateViewModel(
  minDate: LocalDate,
  maxDate: LocalDate,
) : ViewModel() {
  private val datePickerConfiguration = DatePickerConfiguration(minDate, maxDate)
  val dateValidator = datePickerConfiguration.dateValidator

  private val _uiState: MutableStateFlow<TerminateInsuranceUiState> = MutableStateFlow(
    TerminateInsuranceUiState(
      datePickerState = datePickerConfiguration.datePickerState,
      isLoading = false,
    ),
  )
  val uiState: StateFlow<TerminateInsuranceUiState> = _uiState.asStateFlow()
}

// todo change with generic DatePickerUiState
private class DatePickerConfiguration(minDate: LocalDate, maxDate: LocalDate) {
  private val minDateInMillis = minDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  private val maxDateInMillis = maxDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  private val yearRange = minDate.year..maxDate.year

  val datePickerState = DatePickerState(
    initialSelectedDateMillis = null,
    initialDisplayedMonthMillis = null,
    yearRange = yearRange,
    initialDisplayMode = DisplayMode.Picker,
  )
  val dateValidator: (Long) -> Boolean = { selectedDateEpochMillis ->
    selectedDateEpochMillis in minDateInMillis..maxDateInMillis
  }
}

internal data class TerminateInsuranceUiState(
  val datePickerState: DatePickerState,
  val isLoading: Boolean,
) {
  val canSubmit: Boolean
    @Composable
    get() = remember(
      datePickerState.selectedDateMillis,
      isLoading,
    ) { canSubmitSelectedDate() }
}

private fun TerminateInsuranceUiState.canSubmitSelectedDate(): Boolean {
  return datePickerState.selectedDateMillis != null && !isLoading
}
