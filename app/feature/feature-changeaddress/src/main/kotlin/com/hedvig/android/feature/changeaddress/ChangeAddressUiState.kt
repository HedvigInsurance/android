package com.hedvig.android.feature.changeaddress

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.runtime.Stable
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.changeaddress.data.AddressId
import com.hedvig.android.feature.changeaddress.data.ExtraBuilding
import com.hedvig.android.feature.changeaddress.data.ExtraBuildingType
import com.hedvig.android.feature.changeaddress.data.HousingType
import com.hedvig.android.feature.changeaddress.data.MoveIntentId
import com.hedvig.android.feature.changeaddress.data.MoveQuote
import com.hedvig.android.feature.changeaddress.data.MoveResult
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

internal data class ChangeAddressUiState(
  val moveIntentId: MoveIntentId? = null,
  val street: ValidatedInput<String?> = ValidatedInput("test"),
  val postalCode: ValidatedInput<String?> = ValidatedInput("12345"),
  val squareMeters: ValidatedInput<String?> = ValidatedInput("123"),
  val yearOfConstruction: ValidatedInput<String?> = ValidatedInput("1981"),
  val ancillaryArea: ValidatedInput<String?> = ValidatedInput("120"),
  val numberOfBathrooms: ValidatedInput<String?> = ValidatedInput("1"),
  val movingDate: ValidatedInput<LocalDate?> = ValidatedInput(null),
  val numberCoInsured: ValidatedInput<String?> = ValidatedInput("2"),
  val housingType: ValidatedInput<HousingType?> = ValidatedInput(null),
  val extraBuildingTypes: List<ExtraBuildingType> = emptyList(),
  val extraBuildings: List<ExtraBuilding> = listOf(),
  val datePickerUiState: DatePickerUiState? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = false,
  val moveFromAddressId: AddressId? = null,
  val quotes: List<MoveQuote> = emptyList(),
  val successfulMoveResult: MoveResult? = null,
) {

  val isHousingTypeValid: Boolean
    get() = housingType.input != null

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
      yearOfConstruction = yearOfConstruction.copy(
        errorMessageRes = if (housingType.input == HousingType.VILLA && !yearOfConstruction.isPresent) {
          hedvig.resources.R.string.CHANGE_ADDRESS_YEAR_OF_CONSTRUCTION_ERROR
        } else {
          null
        },
      ),
      ancillaryArea = ancillaryArea.copy(
        errorMessageRes = if (housingType.input == HousingType.VILLA && !ancillaryArea.isPresent) {
          hedvig.resources.R.string.CHANGE_ADDRESS_ANCILLARY_AREA_ERROR
        } else {
          null
        },
      ),
      numberOfBathrooms = numberOfBathrooms.copy(
        errorMessageRes = if (housingType.input == HousingType.VILLA && !numberOfBathrooms.isPresent) {
          hedvig.resources.R.string.CHANGE_ADDRESS_BATHROOMS_ERROR
        } else {
          null
        },
      ),
    )
  }
}

@Stable
internal class DatePickerUiState(
  initiallySelectedDate: LocalDate?,
  minDate: LocalDate = LocalDate(1900, 1, 1),
  maxDate: LocalDate = LocalDate(2100, 1, 1),
) {
  private val minDateInMillis = minDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  private val maxDateInMillis = maxDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

  val datePickerState = DatePickerState(
    initialSelectedDateMillis = initiallySelectedDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds(),
    initialDisplayedMonthMillis = null,
    yearRange = minDate.year..maxDate.year,
    initialDisplayMode = DisplayMode.Picker,
  )

  fun validateDate(selectedDateEpochMillis: Long): Boolean {
    return selectedDateEpochMillis in minDateInMillis..maxDateInMillis
  }
}
