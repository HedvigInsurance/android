package com.hedvig.android.feature.travelcertificate

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateResult
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

data class TravelCertificateInputState(
  val contractId: String? = null,
  val email: ValidatedInput<String?> = ValidatedInput(null),
  val travelDate: ValidatedInput<LocalDate?> = ValidatedInput(null),
  val coInsured: ValidatedInput<List<CoInsured>> = ValidatedInput(emptyList()),
  val maximumCoInsured: Int? = null,
  val includeMember: Boolean = false,
  val datePickerState: DatePickerState? = null,
  val dateValidator: (Long) -> Boolean = { false },
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
) {
  val isInputValid: Boolean
    get() {
      return email.errorMessageRes == null &&
        travelDate.errorMessageRes == null &&
        coInsured.errorMessageRes == null
    }

  fun validateInput(): TravelCertificateInputState {
    return copy(
      email = email.copy(
        errorMessageRes = if (!email.isPresent || email.input?.isBlank() == true) {
          hedvig.resources.R.string.CHANGE_ADDRESS_LIVING_SPACE_ERROR
        } else {
          null
        },
      ),
      travelDate = travelDate.copy(
        errorMessageRes = if (!travelDate.isPresent) {
          hedvig.resources.R.string.CHANGE_ADDRESS_LIVING_SPACE_ERROR
        } else {
          null
        },
      ),
      coInsured = coInsured.copy(
        errorMessageRes = if ((!coInsured.isPresent || coInsured.input.isEmpty()) && !includeMember) {
          hedvig.resources.R.string.CHANGE_ADDRESS_LIVING_SPACE_ERROR
        } else {
          null
        },
      ),
    )
  }
}

@Serializable
data class CoInsured(
  val id: String,
  val name: String,
  val ssn: String,
)
