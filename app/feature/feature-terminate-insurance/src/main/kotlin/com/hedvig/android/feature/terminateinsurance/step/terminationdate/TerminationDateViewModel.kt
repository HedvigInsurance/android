package com.hedvig.android.feature.terminateinsurance.step.terminationdate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import com.hedvig.android.design.system.hedvig.datepicker.HedvigDatePickerState
import com.hedvig.android.feature.terminateinsurance.navigation.TerminationDateParameters
import com.hedvig.android.language.LanguageService
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

internal class TerminationDateViewModel(
  parameters: TerminationDateParameters,
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
      exposureName = parameters.commonParams.exposureName,
      displayName = parameters.commonParams.insuranceDisplayName,
      isCheckBoxChecked = false,
    ),
  )
  val uiState: StateFlow<TerminateInsuranceUiState> = _uiState.asStateFlow()

  fun changeSelectedDate(date: Long?) {
    _uiState.value = uiState.value.copy(
      datePickerState =
        uiState.value.datePickerState.copy(selectedDateMillis = date),
    )
  }

  fun changeCheckBoxState() {
    val isCheckedNow = uiState.value.isCheckBoxChecked
    _uiState.value = uiState.value.copy(isCheckBoxChecked = !isCheckedNow)
  }
}

// todo change with generic DatePickerUiState
private class DatePickerConfiguration(locale: Locale, minDate: LocalDate, maxDate: LocalDate) {
  private val minDateInMillis = minDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  private val maxDateInMillis = maxDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  private val yearRange = minDate.year..maxDate.year

  val datePickerState = HedvigDatePickerState(
    locale = locale,
    selectedDateMillis = null,
    displayedMonthMillis = null,
    yearRange = yearRange,
    minDateInMillis = minDateInMillis,
    maxDateInMillis = maxDateInMillis,
  )
}

internal data class TerminateInsuranceUiState(
  val datePickerState: HedvigDatePickerState,
  val isLoading: Boolean,
  val exposureName: String,
  val displayName: String,
  val isCheckBoxChecked: Boolean,
) {
  val canSubmit: Boolean
    @Composable
    get() = remember(
      datePickerState.selectedDateMillis,
      isLoading,
      isCheckBoxChecked,
    ) { canSubmitSelectedDate() }
}

private fun TerminateInsuranceUiState.canSubmitSelectedDate(): Boolean {
  return datePickerState.selectedDateMillis != null && !isLoading && isCheckBoxChecked
}
