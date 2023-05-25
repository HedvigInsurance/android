import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.travelcertificate.CoInsured
import com.hedvig.android.feature.travelcertificate.TravelCertificateInputState
import com.hedvig.android.feature.travelcertificate.data.CreateTravelCertificateUseCase
import com.hedvig.android.feature.travelcertificate.data.GetTravelCertificateSpecificationsUseCase
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class GenerateTravelCertificateViewModel(
  private val getTravelCertificateSpecificationsUseCase: GetTravelCertificateSpecificationsUseCase,
  private val createTravelCertificateUseCase: CreateTravelCertificateUseCase,
) : ViewModel() {
  private val _uiState: MutableStateFlow<TravelCertificateInputState> = MutableStateFlow(TravelCertificateInputState())

  val uiState: StateFlow<TravelCertificateInputState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      getTravelCertificateSpecificationsUseCase
        .invoke()
        .fold(
          ifLeft = { errorMessage -> _uiState.update { TravelCertificateInputState(errorMessage = errorMessage.message) } },
          ifRight = { result -> _uiState.update { createUiState(result) } },
        )
    }
  }

  private fun createUiState(result: TravelCertificateResult) = when (result) {
    TravelCertificateResult.NotEligible -> TravelCertificateInputState(errorMessage = "Not eligible")
    is TravelCertificateResult.TravelCertificateSpecifications -> {
      val datePickerState = DatePickerState(
        initialSelectedDateMillis = null,
        initialDisplayedMonthMillis = null,
        yearRange = result.dateRange.start.year.rangeTo(result.dateRange.endInclusive.year),
        initialDisplayMode = DisplayMode.Picker,
      )

      TravelCertificateInputState(
        contractId = result.contractId,
        email = ValidatedInput(result.email),
        maximumCoInsured = result.numberOfCoInsured,
        datePickerState = datePickerState,
        dateValidator = { date ->
          val selectedDate = Instant.fromEpochMilliseconds(date).toLocalDateTime(TimeZone.currentSystemDefault()).date
          result.dateRange.contains(selectedDate)
        },
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
    _uiState.update { it.copy(travelDate = ValidatedInput(localDate)) }
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
          startDate = state.travelDate.input!!,
          isMemberIncluded = state.includeMember,
          coInsured = state.coInsured.input,
          email = state.email.input!!,
        ).fold(
          ifLeft = { errorMessage ->
            _uiState.update {
              TravelCertificateInputState(
                errorMessage = errorMessage.message,
                isLoading = false,
              )
            }
          },
          ifRight = { url ->
            _uiState.update { it.copy(travelCertificateUrl = url) }
          },
        )
      }
    }
  }

  fun canAddCoInsured(): Boolean {
    val maximumCoInsured = uiState.value.maximumCoInsured
    return maximumCoInsured != null && uiState.value.coInsured.input.size < maximumCoInsured
  }

  fun onMaxCoInsureAdded() {
    val maximumCoInsured = uiState.value.maximumCoInsured
    val message = if (maximumCoInsured != null && maximumCoInsured > 0) {
      "Can not add more than $maximumCoInsured co-insured"
    } else {
      "Can not add any co-insured"
    }
    _uiState.update { it.copy(errorMessage = message) }
  }
}

fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
  return map {
    if (block(it)) newValue else it
  }
}
