package com.hedvig.android.feature.travelcertificate

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateResult
import kotlinx.datetime.LocalDate

data class TravelCertificateUiState(
  val email: ValidatedInput<String?>,
  val travelDate: ValidatedInput<LocalDate?>,
  val coInsured: ValidatedInput<List<CoInsured>>,
  val includeMember: Boolean = false,
  val travelCertificateSpecifications: TravelCertificateResult.TravelCertificateSpecifications,
  val isLoading: Boolean = true,
  val errorMessage: String? = null,
) {
  val datePickerState: DatePickerState = DatePickerState(
    initialSelectedDateMillis = null,
    initialDisplayedMonthMillis = null,
    yearRange = 2023..2054,
    initialDisplayMode = DisplayMode.Picker,
  )
}

data class CoInsured(
  val id: String,
  val name: String,
  val ssn: String,
)
