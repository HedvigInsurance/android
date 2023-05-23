import androidx.lifecycle.ViewModel
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.travelcertificate.CoInsured
import com.hedvig.android.feature.travelcertificate.TravelCertificateUiState
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class GenerateTravelCertificateViewModel(
  email: String?,
  travelCertificateSpecifications: TravelCertificateResult.TravelCertificateSpecifications,
) : ViewModel() {
  private val _uiState: MutableStateFlow<TravelCertificateUiState> = MutableStateFlow(
    TravelCertificateUiState(
      email = ValidatedInput(email),
      travelCertificateSpecifications = travelCertificateSpecifications,
    ),
  )

  val uiState: StateFlow<TravelCertificateUiState> = _uiState.asStateFlow()

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
    if (_uiState.value.isInputValid) {
      // Create travel certificate
    }
  }
}

fun <T> List<T>.replace(newValue: T, block: (T) -> Boolean): List<T> {
  return map {
    if (block(it)) newValue else it
  }
}
