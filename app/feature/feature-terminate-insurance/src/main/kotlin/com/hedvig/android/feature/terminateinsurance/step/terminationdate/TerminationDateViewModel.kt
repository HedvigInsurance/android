package com.hedvig.android.feature.terminateinsurance.step.terminationdate

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationDataParameters
import com.hedvig.android.language.LanguageService
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

internal class TerminationDateViewModel(
  parameters: TerminationDataParameters,
  languageService: LanguageService,
) : ViewModel() {
  private val datePickerConfiguration = DatePickerConfiguration(
    languageService.getLocale(),
    parameters.minDate,
    parameters.maxDate,
  )

  private val _uiState: MutableStateFlow<TerminateInsuranceUiState> = MutableStateFlow(
    TerminateInsuranceUiState(
      datePickerState = datePickerConfiguration.datePickerState,
      isLoading = false,
      exposureName = parameters.exposureName,
      displayName = parameters.insuranceDisplayName,
    ),
  )
  val uiState: StateFlow<TerminateInsuranceUiState> = _uiState.asStateFlow()
}

// todo change with generic DatePickerUiState
private class DatePickerConfiguration(locale: Locale, minDate: LocalDate, maxDate: LocalDate) {
  private val minDateInMillis = minDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  private val maxDateInMillis = maxDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  private val yearRange = minDate.year..maxDate.year

  val datePickerState = DatePickerState(
    locale = locale,
    initialSelectedDateMillis = null,
    initialDisplayedMonthMillis = null,
    yearRange = yearRange,
    initialDisplayMode = DisplayMode.Picker,
    selectableDates = object : SelectableDates {
      override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis in minDateInMillis..maxDateInMillis

      override fun isSelectableYear(year: Int): Boolean = year in yearRange
    },
  )
}

internal data class TerminateInsuranceUiState(
  val datePickerState: DatePickerState,
  val isLoading: Boolean,
  val exposureName: String,
  val displayName: String,
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
