package com.hedvig.android.odyssey.step.dateofoccurrencepluslocation

import androidx.compose.material3.DatePickerState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.odyssey.data.ClaimFlowRepository
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.navigation.LocationOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

internal class DateOfOccurrencePlusLocationViewModel(
  initialDateOfOccurrence: LocalDate?,
  maxDate: LocalDate,
  val selectedLocation: String?,
  val locationOptions: List<LocationOption>,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {

  private val datePickerConfiguration = DatePickerConfiguration(initialDateOfOccurrence, maxDate)
  val dateValidator = datePickerConfiguration.dateValidator

  private val _uiState = MutableStateFlow(
    DateOfOccurrencePlusLocationUiState.fromInitialSelection(
      datePickerConfiguration.datePickerState,
      selectedLocation,
      locationOptions,
    ),
  )
  val uiState = _uiState.asStateFlow()

  fun clearDateSelection() {
    @Suppress("INVISIBLE_MEMBER") // Resetting the date exists in material3 1.1.0-alpha08, for now access internal code
    datePickerConfiguration.datePickerState.selectedDate = null
  }

  fun selectLocationOption(selectedLocationOption: LocationOption) {
    _uiState.update { oldUiState ->
      val selectedValueExistsInOptions = selectedLocationOption in locationOptions
      val locationIsAlreadySelected = oldUiState.selectedLocation == selectedLocationOption
      if (locationIsAlreadySelected || !selectedValueExistsInOptions) {
        oldUiState.copy(selectedLocation = null)
      } else {
        oldUiState.copy(selectedLocation = selectedLocationOption)
      }
    }
  }

  fun submitDateOfOccurrenceAndLocation() {
    val uiState = _uiState.value
    val selectedDateOfOccurrence = uiState.datePickerState.selectedDateMillis?.let {
      Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date
    }
    val selectedLocation = uiState.selectedLocation
    if (!uiState.canSubmit) return
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      claimFlowRepository.submitDateOfOccurrenceAndLocation(selectedDateOfOccurrence, selectedLocation?.value).fold(
        ifLeft = {
          _uiState.update {
            it.copy(isLoading = false, error = true)
          }
        },
        ifRight = { claimFlowStep ->
          _uiState.update {
            it.copy(isLoading = false, nextStep = claimFlowStep)
          }
        },
      )
    }
  }

  fun showedError() {
    _uiState.update {
      it.copy(error = false)
    }
  }

  fun handledNextStepNavigation() {
    _uiState.update { it.copy(nextStep = null) }
  }
}

private class DatePickerConfiguration(
  dateOfOccurrence: LocalDate?,
  maxDate: LocalDate,
) {
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

internal data class DateOfOccurrencePlusLocationUiState(
  val datePickerState: DatePickerState,
  val locationOptions: List<LocationOption>,
  val selectedLocation: LocationOption?,
  val isLoading: Boolean = false,
  val error: Boolean = false,
  val nextStep: ClaimFlowStep? = null,
) {
  val canSubmit: Boolean = !isLoading && !error && nextStep == null

  companion object {
    fun fromInitialSelection(
      datePickerState: DatePickerState,
      initialSelectedLocation: String?,
      locationOptions: List<LocationOption>,
    ): DateOfOccurrencePlusLocationUiState {
      val selectedLocation = locationOptions
        .firstOrNull { it.value == initialSelectedLocation }
      return DateOfOccurrencePlusLocationUiState(
        datePickerState,
        locationOptions,
        selectedLocation,
      )
    }
  }
}
