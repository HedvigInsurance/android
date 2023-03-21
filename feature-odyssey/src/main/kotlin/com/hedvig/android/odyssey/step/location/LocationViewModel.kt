package com.hedvig.android.odyssey.step.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.odyssey.data.ClaimFlowRepository
import com.hedvig.android.odyssey.data.ClaimFlowStep
import com.hedvig.android.odyssey.navigation.LocationOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class LocationViewModel(
  initialSelectedLocation: String?,
  private val locationOptions: List<LocationOption>,
  private val claimFlowRepository: ClaimFlowRepository,
) : ViewModel() {

  private val _uiState =
    MutableStateFlow(LocationUiState.fromInitialSelection(initialSelectedLocation, locationOptions))
  val uiState = _uiState.asStateFlow()

  fun selectLocationOption(selectedLocationValue: String) {
    _uiState.update { oldUiState ->
      val selectedValueExistsInOptions = selectedLocationValue in locationOptions.map { it.value }
      val locationIsAlreadySelected = oldUiState.selectedLocation == selectedLocationValue
      if (locationIsAlreadySelected || !selectedValueExistsInOptions) {
        oldUiState.copy(selectedLocation = null)
      } else {
        oldUiState.copy(selectedLocation = selectedLocationValue)
      }
    }
  }

  fun showedError() {
    _uiState.update {
      it.copy(error = false)
    }
  }

  fun submitLocation() {
    val uiState = _uiState.value
    val selectedLocation = uiState.selectedLocation
    if (!uiState.canSubmit) return
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      claimFlowRepository.submitLocation(selectedLocation).fold(
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

  fun handledNextStepNavigation() {
    _uiState.update { it.copy(nextStep = null) }
  }
}

internal data class LocationUiState(
  val locationOptions: List<LocationOption>,
  val selectedLocation: String?,
  val isLoading: Boolean = false,
  val error: Boolean = false,
  val nextStep: ClaimFlowStep? = null,
) {
  val canSubmit: Boolean = selectedLocation != null && !isLoading && !error && nextStep == null

  companion object {
    fun fromInitialSelection(
      initialSelectedLocation: String?,
      locationOptions: List<LocationOption>,
    ): LocationUiState {
      val selectedLocation = locationOptions
        .firstOrNull { it.value == initialSelectedLocation }
        ?.value
      return LocationUiState(
        locationOptions,
        selectedLocation,
      )
    }
  }
}
