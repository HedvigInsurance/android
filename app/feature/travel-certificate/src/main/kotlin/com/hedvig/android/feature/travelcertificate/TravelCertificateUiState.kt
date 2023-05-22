package com.hedvig.android.feature.travelcertificate

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateResult
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

data class TravelCertificateUiState(
  val email: ValidatedInput<String?>,
  val travelDate: ValidatedInput<LocalDate?> = ValidatedInput(null),
  val coInsured: ValidatedInput<List<CoInsured>> = ValidatedInput(emptyList()),
  val includeMember: Boolean = false,
  val travelCertificateSpecifications: TravelCertificateResult.TravelCertificateSpecifications,
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
) {
  val datePickerState: DatePickerState = DatePickerState(
    initialSelectedDateMillis = null,
    initialDisplayedMonthMillis = null,
    yearRange = 2023..2054,
    initialDisplayMode = DisplayMode.Picker,
  )
}

@Serializable
data class CoInsured(
  val id: String,
  val name: String,
  val ssn: String,
)
