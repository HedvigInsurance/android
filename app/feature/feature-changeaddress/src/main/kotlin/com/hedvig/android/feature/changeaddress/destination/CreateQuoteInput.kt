package com.hedvig.android.feature.changeaddress.destination

import com.hedvig.android.feature.changeaddress.data.AddressId
import com.hedvig.android.feature.changeaddress.data.AddressInput
import com.hedvig.android.feature.changeaddress.data.ExtraBuilding
import com.hedvig.android.feature.changeaddress.data.HousingType
import com.hedvig.android.feature.changeaddress.data.MoveIntentId
import com.hedvig.android.feature.changeaddress.data.QuoteInput
import kotlinx.datetime.LocalDate

internal fun createQuoteInput(
  housingType: HousingType?,
  isSublet: Boolean,
  isStudent: Boolean,
  moveIntentId: String?,
  street: String?,
  postalCode: String?,
  moveFromAddressId: AddressId?,
  movingDate: LocalDate?,
  numberInsured: String?,
  squareMeters: String?,
  yearOfConstruction: String?,
  ancillaryArea: String?,
  numberOfBathrooms: String?,
  extraBuildings: List<ExtraBuilding>,
  ): QuoteInput {
  return when (housingType) {
    HousingType.APARTMENT_RENT,
    HousingType.APARTMENT_OWN,
    -> QuoteInput.ApartmentInput(
      moveIntentId = MoveIntentId(moveIntentId!!),
      address = AddressInput(
        street = street!!,
        postalCode = postalCode!!,
      ),
      moveFromAddressId = moveFromAddressId!!,
      movingDate = movingDate!!,
      numberCoInsured = numberInsured!!.toInt() - 1,
      squareMeters = squareMeters!!.toInt(),
      apartmentOwnerType = housingType,
      isStudent = isStudent,
    )

    HousingType.VILLA -> QuoteInput.VillaInput(
      moveIntentId = MoveIntentId(moveIntentId!!),
      address = AddressInput(
        street = street!!,
        postalCode = postalCode!!,
      ),
      moveFromAddressId = moveFromAddressId!!,
      movingDate = movingDate!!,
      numberCoInsured = numberInsured!!.toInt() - 1,
      squareMeters = squareMeters!!.toInt(),
      apartmentOwnerType = housingType,
      yearOfConstruction = yearOfConstruction!!.toInt(),
      ancillaryArea = ancillaryArea!!.toInt(),
      numberOfBathrooms = numberOfBathrooms!!.toInt(),
      extraBuildings = extraBuildings,
      isStudent = isStudent,
      isSubleted = isSublet,
    )

    null -> throw IllegalArgumentException("No housing type found when creating input")
  }
}
