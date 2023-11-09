package com.hedvig.android.feature.odyssey.step.dateofoccurrencepluslocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.data.claimflow.ClaimFlowDestination
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.LocationOption
import com.hedvig.android.feature.odyssey.ui.DatePickerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class DateOfOccurrencePlusLocationViewModel(
  private val dateOfOccurrencePlusLocation: ClaimFlowDestination.DateOfOccurrencePlusLocation,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {
  private val _uiState = MutableStateFlow(
    DateOfOccurrencePlusLocationUiState.fromInitialSelection(
      DatePickerUiState(
        initiallySelectedDate = dateOfOccurrencePlusLocation.dateOfOccurrence,
        maxDate = dateOfOccurrencePlusLocation.maxDate,
      ),
      dateOfOccurrencePlusLocation.selectedLocation,
      dateOfOccurrencePlusLocation.locationOptions,
    ),
  )
  val uiState = _uiState.asStateFlow()

  fun selectLocationOption(selectedLocationOption: LocationOption) {
    _uiState.update { oldUiState ->
      val selectedValueExistsInOptions = selectedLocationOption in dateOfOccurrencePlusLocation.locationOptions
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
    val selectedDateOfOccurrence = uiState.datePickerUiState.datePickerState.selectedDateMillis?.let {
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

internal data class DateOfOccurrencePlusLocationUiState(
  val datePickerUiState: DatePickerUiState,
  val locationOptions: List<LocationOption>,
  val selectedLocation: LocationOption?,
  val isLoading: Boolean = false,
  val error: Boolean = false,
  val nextStep: ClaimFlowStep? = null,
) {
  val canSubmit: Boolean = !isLoading && !error && nextStep == null

  companion object {
    fun fromInitialSelection(
      datePickerUiState: DatePickerUiState,
      initialSelectedLocation: String?,
      locationOptions: List<LocationOption>,
    ): DateOfOccurrencePlusLocationUiState {
      val selectedLocation = locationOptions
        .firstOrNull { it.value == initialSelectedLocation }
      return DateOfOccurrencePlusLocationUiState(
        datePickerUiState,
        locationOptions,
        selectedLocation,
      )
    }
  }
}
