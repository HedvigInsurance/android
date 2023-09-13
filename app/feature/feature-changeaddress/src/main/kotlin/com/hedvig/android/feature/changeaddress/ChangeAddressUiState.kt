package com.hedvig.android.feature.changeaddress

import HousingType
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.changeaddress.data.AddressId
import com.hedvig.android.feature.changeaddress.data.MoveIntentId
import com.hedvig.android.feature.changeaddress.data.MoveQuote
import com.hedvig.android.feature.changeaddress.data.MoveResult
import kotlinx.datetime.LocalDate

data class ChangeAddressUiState(
  val moveIntentId: MoveIntentId? = null,
  val street: ValidatedInput<String?> = ValidatedInput(null),
  val postalCode: ValidatedInput<String?> = ValidatedInput(null),
  val squareMeters: ValidatedInput<String?> = ValidatedInput(null),
  val movingDate: ValidatedInput<LocalDate?> = ValidatedInput(null),
  val numberCoInsured: ValidatedInput<String?> = ValidatedInput(null),
  val housingType: ValidatedInput<HousingType?> = ValidatedInput(null),
  val moveRange: ClosedRange<LocalDate>? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = true,
  val moveFromAddressId: AddressId? = null,
  val quotes: List<MoveQuote> = emptyList(),
  val successfulMoveResult: MoveResult? = null,
) {

  val datePickerState: DatePickerState = DatePickerState(
    initialSelectedDateMillis = null,
    initialDisplayedMonthMillis = null,
    yearRange = 2023..2054,
    initialDisplayMode = DisplayMode.Picker,
  )

  val isHousingTypeValid: Boolean
    get() = housingType.input != null && housingType.input != HousingType.VILLA

  val isInputValid: Boolean
    get() {
      return street.errorMessageRes == null &&
        postalCode.errorMessageRes == null &&
        squareMeters.errorMessageRes == null &&
        movingDate.errorMessageRes == null &&
        numberCoInsured.errorMessageRes == null &&
        housingType.errorMessageRes == null
    }

  fun validateInput(): ChangeAddressUiState {
    return copy(
      street = street.copy(
        errorMessageRes = if (!street.isPresent || street.input?.isBlank() == true) {
          hedvig.resources.R.string.CHANGE_ADDRESS_STREET_ERROR
        } else {
          null
        },
      ),
      postalCode = postalCode.copy(
        errorMessageRes = if (!postalCode.isPresent || postalCode.input?.isBlank() == true) {
          hedvig.resources.R.string.CHANGE_ADDRESS_POSTAL_CODE_ERROR
        } else {
          null
        },
      ),
      squareMeters = squareMeters.copy(
        errorMessageRes = if (!squareMeters.isPresent || squareMeters.input?.isBlank() == true) {
          hedvig.resources.R.string.CHANGE_ADDRESS_LIVING_SPACE_ERROR
        } else {
          null
        },
      ),
      movingDate = movingDate.copy(
        errorMessageRes = if (!movingDate.isPresent) {
          hedvig.resources.R.string.CHANGE_ADDRESS_MOVING_DATE_ERROR
        } else {
          null
        },
      ),
      numberCoInsured = numberCoInsured.copy(
        errorMessageRes = if (!numberCoInsured.isPresent) {
          hedvig.resources.R.string.CHANGE_ADDRESS_CO_INSURED_ERROR
        } else {
          null
        },
      ),
      housingType = housingType.copy(
        errorMessageRes = if (!housingType.isPresent) {
          hedvig.resources.R.string.CHANGE_ADDRESS_HOUSING_TYPE_ERROR
        } else {
          null
        },
      ),
    )
  }
}
