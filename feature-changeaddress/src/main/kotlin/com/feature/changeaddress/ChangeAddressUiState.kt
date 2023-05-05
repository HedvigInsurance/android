package com.feature.changeaddress

import ApartmentOwnerType
import androidx.annotation.NonNull
import com.feature.changeaddress.data.AddressId
import com.feature.changeaddress.data.MoveIntentId
import com.feature.changeaddress.data.MoveQuote
import com.feature.changeaddress.data.MoveResult
import kotlinx.datetime.LocalDate

data class ChangeAddressUiState(
  val moveIntentId: MoveIntentId? = null,
  val street: ValidatedInput<String?> = ValidatedInput(null),
  val postalCode: ValidatedInput<String?> = ValidatedInput(null),
  val squareMeters: ValidatedInput<String?> = ValidatedInput(null),
  val movingDate: ValidatedInput<LocalDate?> = ValidatedInput(LocalDate(2023, 6, 1)),
  val numberCoInsured: ValidatedInput<Int?> = ValidatedInput(null),
  val apartmentOwnerType: ValidatedInput<ApartmentOwnerType?> = ValidatedInput(null),
  val moveRange: ClosedRange<LocalDate>? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = true,
  val moveFromAddressId: AddressId? = null,
  val quotes: List<MoveQuote> = emptyList(),
  val successfulMoveResult: MoveResult? = null,
) {
  val isValid: Boolean
    get() {
      return street.errorMessage == null &&
        postalCode.errorMessage == null &&
        squareMeters.errorMessage == null &&
        movingDate.errorMessage == null &&
        numberCoInsured.errorMessage == null &&
        apartmentOwnerType.errorMessage == null
    }

  fun validateInput(): ChangeAddressUiState {
    return copy(
      street = street.copy(
        errorMessage = if (!street.isPresent || street.input?.isBlank() == true) {
          "Please enter a street"
        } else null,
      ),
      postalCode = postalCode.copy(
        errorMessage = if (!postalCode.isPresent || postalCode.input?.isBlank() == true) {
          "Please enter a postal code"
        } else null,
      ),
      squareMeters = squareMeters.copy(
        errorMessage = if (!squareMeters.isPresent || squareMeters.input?.isBlank() == true) {
          "Please enter square meters"
        } else null,
      ),
      movingDate = movingDate.copy(
        errorMessage = if (!movingDate.isPresent) {
          "Please select a moving date"
        } else null,
      ),
      numberCoInsured = numberCoInsured.copy(
        errorMessage = if (!numberCoInsured.isPresent) {
          "Please select number of co-insured"
        } else null,
      ),
      apartmentOwnerType = apartmentOwnerType.copy(
        errorMessage = if (!apartmentOwnerType.isPresent) {
          "Please select owner type"
        } else null,
      ),
    )
  }
}

data class ValidatedInput<T>(
  val input: T,
  val errorMessage: String? = null,
) {
  val isPresent: Boolean
    get() = input != null
}
