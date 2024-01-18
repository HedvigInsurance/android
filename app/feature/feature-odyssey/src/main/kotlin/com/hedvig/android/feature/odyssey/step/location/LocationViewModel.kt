package com.hedvig.android.feature.odyssey.step.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.data.claimflow.ClaimFlowRepository
import com.hedvig.android.data.claimflow.ClaimFlowStep
import com.hedvig.android.data.claimflow.LocationOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
  val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

  fun selectLocationOption(selectedLocation: LocationOption) {
    _uiState.update { oldUiState ->
      val selectedValueExistsInOptions = selectedLocation in locationOptions
      val locationIsAlreadySelected = oldUiState.selectedLocation == selectedLocation
      if (locationIsAlreadySelected || !selectedValueExistsInOptions) {
        oldUiState.copy(selectedLocation = null)
      } else {
        oldUiState.copy(selectedLocation = selectedLocation)
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
    if (selectedLocation == null || !uiState.canSubmit) return
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      claimFlowRepository.submitLocation(selectedLocation.value).fold(
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
  val selectedLocation: LocationOption?,
  val isLoading: Boolean = false,
  val error: Boolean = false,
  val nextStep: ClaimFlowStep? = null,
) {
  val canSubmit: Boolean = selectedLocation != null && !error && nextStep == null

  companion object {
    fun fromInitialSelection(
      initialSelectedLocation: String?,
      locationOptions: List<LocationOption>,
    ): LocationUiState {
      val selectedLocation = locationOptions
        .firstOrNull { it.value == initialSelectedLocation }
      return LocationUiState(
        locationOptions,
        selectedLocation,
      )
    }
  }
}
