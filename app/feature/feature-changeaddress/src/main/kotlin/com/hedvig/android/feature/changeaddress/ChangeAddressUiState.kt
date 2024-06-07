package com.hedvig.android.feature.changeaddress

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Stable
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.changeaddress.data.AddressId
import com.hedvig.android.feature.changeaddress.data.ExtraBuilding
import com.hedvig.android.feature.changeaddress.data.ExtraBuildingType
import com.hedvig.android.feature.changeaddress.data.HousingType
import com.hedvig.android.feature.changeaddress.data.MoveIntentId
import com.hedvig.android.feature.changeaddress.data.MoveQuote
import com.hedvig.android.feature.changeaddress.data.SuccessfulMove
import java.util.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.serialization.Serializable

internal data class ChangeAddressUiState(
  val moveIntentId: MoveIntentId? = null,
  val street: ValidatedInput<String?> = ValidatedInput(null),
  val postalCode: ValidatedInput<String?> = ValidatedInput(null),
  val squareMeters: ValidatedInput<String?> = ValidatedInput(null),
  val yearOfConstruction: ValidatedInput<String?> = ValidatedInput(null),
  val ancillaryArea: ValidatedInput<String?> = ValidatedInput(null),
  val numberOfBathrooms: ValidatedInput<String?> = ValidatedInput(null),
  val movingDate: ValidatedInput<LocalDate?> = ValidatedInput(null),
  val numberInsured: ValidatedInput<String?> = ValidatedInput(null),
  val housingType: ValidatedInput<HousingType?> = ValidatedInput(null),
  val isSublet: ValidatedInput<Boolean> = ValidatedInput(false),
  val isStudent: Boolean = false,
  val isEligibleForStudent: Boolean = false,
  val maxNumberCoInsured: Int? = null,
  val maxSquareMeters: Int? = null,
  val extraBuildingTypes: List<ExtraBuildingType> = emptyList(),
  val extraBuildings: List<ExtraBuilding> = listOf(),
  val datePickerUiState: DatePickerUiState? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = false,
  val moveFromAddressId: AddressId? = null,
  val quotes: List<MoveQuote> = emptyList(),
  val successfulMoveResult: SuccessfulMove? = null,
  /**
   * When we receive the moveIntentId, we want to navigate to the next step. We keep this signal here so that we can
   * still go back to the previous screen and not introduce an infinite navigation loop
   */
  val navigateToFirstStepAfterHavingReceivedMoveIntentId: Boolean = false,
  /**
   * When we receive some quotes from submitting all the data, we want to navigate to the offer destination. We keep
   * this signal so that we can go back to the previous screen and not introduce an infinite navigation loop.
   */
  val navigateToOfferScreenAfterHavingReceivedQuotes: Boolean = false,
) {
  val isHousingTypeValid: Boolean
    get() = housingType.input != null

  val isAddressInputValid: Boolean
    get() {
      return street.errorMessageRes == null &&
        postalCode.errorMessageRes == null &&
        squareMeters.errorMessageRes == null &&
        isSquareMetersWithinBounds(squareMeters.input?.toIntOrNull()) &&
        movingDate.errorMessageRes == null &&
        numberInsured.errorMessageRes == null &&
        housingType.errorMessageRes == null
    }

  val isHouseInputValid: Boolean
    get() {
      return yearOfConstruction.errorMessageRes == null &&
        ancillaryArea.errorMessageRes == null &&
        numberOfBathrooms.errorMessageRes == null
    }

  fun validateAddressInput(): ChangeAddressUiState {
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
        } else if (squareMeters.isPresent && !isSquareMetersWithinBounds(squareMeters.input!!.toIntOrNull())) {
          hedvig.resources.R.string.CHANGE_ADDRESS_LIVING_SPACE_OVER_LIMIT_ERROR
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
      numberInsured = numberInsured.copy(
        errorMessageRes = if (!numberInsured.isPresent) {
          hedvig.resources.R.string.CHANGE_ADDRESS_CO_INSURED_ERROR
        } else if (numberInsured.isPresent && !isNumberCoInsuredWithinBounds(numberInsured.input!!.toIntOrNull())) {
          hedvig.resources.R.string.CHANGE_ADDRESS_CO_INSURED_MAX_ERROR_ALTERNATIVE
        } else {
          null
        },
      ),
    )
  }

  fun validateHouseInput(): ChangeAddressUiState {
    return copy(
      yearOfConstruction = yearOfConstruction.copy(
        errorMessageRes = if (!yearOfConstruction.isPresent) {
          hedvig.resources.R.string.CHANGE_ADDRESS_YEAR_OF_CONSTRUCTION_ERROR
        } else {
          null
        },
      ),
      ancillaryArea = ancillaryArea.copy(
        errorMessageRes = if (!ancillaryArea.isPresent) {
          hedvig.resources.R.string.CHANGE_ADDRESS_ANCILLARY_AREA_ERROR
        } else {
          null
        },
      ),
      numberOfBathrooms = numberOfBathrooms.copy(
        errorMessageRes = if (!numberOfBathrooms.isPresent) {
          hedvig.resources.R.string.CHANGE_ADDRESS_BATHROOMS_ERROR
        } else {
          null
        },
      ),
    )
  }

  private fun isSquareMetersWithinBounds(squareMeters: Int?): Boolean {
    if (maxSquareMeters == null) {
      return true
    }
    if (squareMeters == null) {
      return false
    }
    return squareMeters <= maxSquareMeters
  }

  private fun isNumberCoInsuredWithinBounds(numberCoInsured: Int?): Boolean {
    if (maxNumberCoInsured == null) {
      return true
    }
    if (numberCoInsured == null) {
      return false
    }
    return numberCoInsured <= maxNumberCoInsured
  }
}

@Stable
internal class DatePickerUiState(
  locale: Locale,
  initiallySelectedDate: LocalDate?,
  minDate: LocalDate = LocalDate(1900, 1, 1),
  maxDate: LocalDate = LocalDate(2100, 1, 1),
) {
  private val minDateInMillis = minDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  private val maxDateInMillis = maxDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
  private val yearRange = minDate.year..maxDate.year

  val datePickerState = DatePickerState(
    locale = locale,
    initialSelectedDateMillis = initiallySelectedDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds(),
    initialDisplayedMonthMillis = null,
    yearRange = yearRange,
    initialDisplayMode = DisplayMode.Picker,
    selectableDates = object : SelectableDates {
      override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis in minDateInMillis..maxDateInMillis

      override fun isSelectableYear(year: Int) = year in yearRange
    },
  )
}

@Serializable
internal data class MovingParameters(
  val moveIntentId: String?,
  val street: String?,
  val postalCode: String?,
  val squareMeters: String?,
  val yearOfConstruction: String?,
  val ancillaryArea: String?,
  val numberOfBathrooms: String?,
  val movingDate: LocalDate?,
  val numberInsured: String?,
  val housingType: HousingType?,
  val isSublet: Boolean,
  val isStudent: Boolean,
  val isEligibleForStudent: Boolean,
  val maxNumberCoInsured: Int?,
  val maxSquareMeters: Int?,
  val extraBuildingTypes: List<ExtraBuildingType>,
  val extraBuildings: List<ExtraBuilding>,
  val moveFromAddressId: AddressId?,
)
