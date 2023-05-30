package com.hedvig.android.feature.travelcertificate

import androidx.compose.material3.DatePickerState
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateResult
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUri
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.serialization.Serializable

data class TravelCertificateInputState(
  val contractId: String? = null,
  val email: ValidatedInput<String?> = ValidatedInput(null),
  val travelDate: LocalDate = LocalDateTime.now().toLocalDate().toKotlinLocalDate(),
  val coInsured: ValidatedInput<List<CoInsured>> = ValidatedInput(emptyList()),
  val maximumCoInsured: Int? = null,
  val includeMember: Boolean = true,
  val datePickerState: DatePickerState? = null,
  val dateValidator: (Long) -> Boolean = { false },
  val daysValid: Int? = null,
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val travelCertificateUrl: TravelCertificateUrl? = null,
  val travelCertificateUri: TravelCertificateUri? = null,
  val infoSections: List<TravelCertificateResult.TraverlCertificateData.InfoSection>? = null,
) {
  val isInputValid: Boolean
    get() {
      return email.errorMessageRes == null &&
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
) {
  fun firstName(): String = name.split(" ").firstOrNull() ?: this.name
}
