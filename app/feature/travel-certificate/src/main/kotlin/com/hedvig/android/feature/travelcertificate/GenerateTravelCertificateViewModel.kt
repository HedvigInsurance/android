package com.hedvig.android.feature.travelcertificate

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.data.travelcertificate.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.data.travelcertificate.TravelCertificateResult
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.DownloadTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class GenerateTravelCertificateViewModel(
  private val getTravelCertificateSpecificationsUseCase: GetTravelCertificateSpecificationsUseCase,
  private val createTravelCertificateUseCase: CreateTravelCertificateUseCase,
  private val downloadTravelCertificateUseCase: DownloadTravelCertificateUseCase,
) : ViewModel() {
  private val _uiState: MutableStateFlow<TravelCertificateInputState> = MutableStateFlow(TravelCertificateInputState())
  val uiState: StateFlow<TravelCertificateInputState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      getTravelCertificateSpecificationsUseCase
        .invoke()
        .fold(
          ifLeft = { errorMessage ->
            _uiState.update {
              TravelCertificateInputState(errorMessage = errorMessage.message)
            }
          },
          ifRight = { result -> _uiState.update { createUiState(result) } },
        )
    }
  }

  private fun createUiState(result: TravelCertificateResult) = when (result) {
    TravelCertificateResult.NotEligible -> TravelCertificateInputState(errorMessage = "Not eligible")
    is TravelCertificateResult.TraverlCertificateData -> {
      val travelSpecification = result.travelCertificateSpecification

      val datePickerState = DatePickerState(
        initialSelectedDateMillis = null,
        initialDisplayedMonthMillis = null,
        yearRange = travelSpecification.dateRange.start.year.rangeTo(travelSpecification.dateRange.endInclusive.year),
        initialDisplayMode = DisplayMode.Picker,
      )

      TravelCertificateInputState(
        contractId = travelSpecification.contractId,
        email = ValidatedInput(travelSpecification.email),
        maximumCoInsured = travelSpecification.numberOfCoInsured,
        datePickerState = datePickerState,
        dateValidator = { date ->
          val selectedDate = Instant.fromEpochMilliseconds(date).toLocalDateTime(TimeZone.currentSystemDefault()).date
          travelSpecification.dateRange.contains(selectedDate)
        },
        daysValid = travelSpecification.maxDurationDays,
        infoSections = result.infoSections,
      )
    }
  }

  fun onErrorDialogDismissed() {
    _uiState.update { it.copy(errorMessage = null) }
  }

  fun onEmailChanged(email: String) {
    _uiState.update { it.copy(email = ValidatedInput(email)) }
  }

  fun onIncludeMemberClicked(includeMember: Boolean) {
    _uiState.update {
      it.copy(
        includeMember = includeMember,
        coInsured = it.coInsured.copy(errorMessageRes = null),
      )
    }
  }

  fun onTravelDateSelected(localDate: LocalDate) {
    _uiState.update { it.copy(travelDate = localDate) }
  }

  fun onAddCoInsured(coInsured: CoInsured) {
    val updatedCoInsured = uiState.value.coInsured.input + coInsured
    _uiState.update {
      it.copy(coInsured = ValidatedInput(updatedCoInsured))
    }
  }

  fun onEditCoInsured(coInsured: CoInsured) {
    val updatedCoInsuredList = uiState.value.coInsured.input.replace(
      newValue = coInsured,
      block = { it.id == coInsured.id },
    )
    _uiState.update { it.copy(coInsured = ValidatedInput(updatedCoInsuredList)) }
  }

  fun onCoInsuredRemoved(coInsuredId: String) {
    val updatedCoInsuredList = uiState.value.coInsured.input.filterNot { it.id == coInsuredId }
    _uiState.update {
      it.copy(coInsured = ValidatedInput(updatedCoInsuredList))
    }
  }

  fun onContinue() {
    _uiState.update { it.validateInput() }
    val state = uiState.value
    if (_uiState.value.isInputValid) {
      viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        createTravelCertificateUseCase.invoke(
          contractId = state.contractId!!,
          startDate = state.travelDate,
          isMemberIncluded = state.includeMember,
          coInsured = state.coInsured.input,
          email = state.email.input!!,
        ).fold(
          ifLeft = { errorMessage ->
            _uiState.update {
              it.copy(
                isLoading = false,
                errorMessage = errorMessage.message,
              )
            }
          },
          ifRight = { url ->
            _uiState.update {
              it.copy(
                isLoading = false,
                travelCertificateUrl = url,
              )
            }
          },
        )
      }
    }
  }

  fun canAddCoInsured(): Boolean {
    val maximumCoInsured = uiState.value.maximumCoInsured
    return maximumCoInsured != null && uiState.value.coInsured.input.size < maximumCoInsured
  }

  fun onDownloadTravelCertificate(url: TravelCertificateUrl) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      downloadTravelCertificateUseCase.invoke(url)
        .fold(
          ifLeft = { errorMessage ->
            _uiState.update {
              it.copy(
                isLoading = false,
                errorMessage = errorMessage.message,
              )
            }
          },
          ifRight = { uri ->
            _uiState.update {
              it.copy(
                isLoading = false,
                travelCertificateUri = uri,
              )
            }
          },
        )
    }
  }
}

private fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
  return map {
    if (block(it)) newValue else it
  }
}
