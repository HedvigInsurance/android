package com.feature.changeaddress.data

import kotlinx.datetime.LocalDate

enum class ApartmentOwnerType {
  RENT, OWN
}

data class CreateQuoteInput(
  val moveIntentId: MoveIntentId,
  val moveFromAddressId: AddressId,
  val address: Address,
  val movingDate: LocalDate,
  val numberCoInsured: Int,
  val squareMeters: Int,
  val apartmentOwnerType: ApartmentOwnerType,
  val isStudent: Boolean,
)
