package com.hedvig.android.feature.changeaddress.data

import com.apollographql.apollo3.api.Optional
import kotlinx.datetime.LocalDate
import octopus.MoveIntentRequestMutation
import octopus.type.MoveApartmentSubType
import octopus.type.MoveExtraBuildingInput
import octopus.type.MoveIntentRequestInput
import octopus.type.MoveToAddressInput
import octopus.type.MoveToApartmentInput
import octopus.type.MoveToHouseInput

internal enum class HousingType {
  APARTMENT_RENT,
  APARTMENT_OWN,
  VILLA,
}

internal fun HousingType.displayNameResource() = when (this) {
  HousingType.APARTMENT_RENT -> hedvig.resources.R.string.CHANGE_ADDRESS_APARTMENT_RENT_LABEL
  HousingType.APARTMENT_OWN -> hedvig.resources.R.string.CHANGE_ADDRESS_APARTMENT_OWN_LABEL
  HousingType.VILLA -> hedvig.resources.R.string.CHANGE_ADDRESS_VILLA_LABEL
}

internal sealed interface QuoteInput {
  fun toMoveIntentRequestMutation(): MoveIntentRequestMutation

  data class ApartmentInput(
    val moveIntentId: MoveIntentId,
    val moveFromAddressId: AddressId,
    val address: AddressInput,
    val movingDate: LocalDate,
    val numberCoInsured: Int,
    val squareMeters: Int,
    val apartmentOwnerType: HousingType,
    val isStudent: Boolean,
  ) : QuoteInput {
    override fun toMoveIntentRequestMutation(): MoveIntentRequestMutation = MoveIntentRequestMutation(
      intentId = moveIntentId.id,
      input = MoveIntentRequestInput(
        moveToAddress = MoveToAddressInput(
          street = address.street,
          postalCode = address.postalCode,
          city = Optional.absent(),
        ),
        moveFromAddressId = moveFromAddressId.id,
        movingDate = movingDate,
        numberCoInsured = numberCoInsured,
        squareMeters = squareMeters,
        apartment = Optional.present(
          MoveToApartmentInput(
            subType = when (apartmentOwnerType) {
              HousingType.APARTMENT_RENT -> MoveApartmentSubType.RENT
              HousingType.APARTMENT_OWN -> MoveApartmentSubType.OWN
              HousingType.VILLA -> throw IllegalArgumentException("Can not create request with villa type")
            },
            isStudent = isStudent,
          ),
        ),
      ),
    )
  }

  data class VillaInput(
    val moveIntentId: MoveIntentId,
    val moveFromAddressId: AddressId,
    val address: AddressInput,
    val movingDate: LocalDate,
    val numberCoInsured: Int,
    val squareMeters: Int,
    val apartmentOwnerType: HousingType,
    val yearOfConstruction: Int,
    val ancillaryArea: Int,
    val numberOfBathrooms: Int,
    val isSubleted: Boolean,
    val extraBuildings: List<ExtraBuilding>,
    val isStudent: Boolean,
  ) : QuoteInput {
    override fun toMoveIntentRequestMutation(): MoveIntentRequestMutation = MoveIntentRequestMutation(
      intentId = moveIntentId.id,
      input = MoveIntentRequestInput(
        moveToAddress = MoveToAddressInput(
          street = address.street,
          postalCode = address.postalCode,
          city = Optional.absent(),
        ),
        moveFromAddressId = moveFromAddressId.id,
        movingDate = movingDate,
        numberCoInsured = numberCoInsured,
        squareMeters = squareMeters,
        house = Optional.present(
          MoveToHouseInput(
            ancillaryArea = ancillaryArea,
            yearOfConstruction = yearOfConstruction,
            numberOfBathrooms = numberOfBathrooms,
            isSubleted = isSubleted,
            extraBuildings = extraBuildings.map {
              MoveExtraBuildingInput(
                area = it.size,
                type = it.type.toMoveExtraBuildingType(),
                hasWaterConnected = it.hasWaterConnected,
              )
            },
          ),
        ),
      ),
    )
  }
}
