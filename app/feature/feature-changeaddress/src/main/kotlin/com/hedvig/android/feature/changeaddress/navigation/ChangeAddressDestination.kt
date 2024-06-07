package com.hedvig.android.feature.changeaddress.navigation

import com.hedvig.android.feature.changeaddress.data.AddressId
import com.hedvig.android.feature.changeaddress.data.ExtraBuilding
import com.hedvig.android.feature.changeaddress.data.ExtraBuildingType
import com.hedvig.android.feature.changeaddress.data.HousingType
import com.kiwi.navigationcompose.typed.Destination
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

internal sealed interface ChangeAddressDestination : Destination {
  @Serializable
  data object SelectHousingType : ChangeAddressDestination

  @Serializable
  data object EnterNewAddress : ChangeAddressDestination

  @Serializable
  data object EnterVillaInformation : ChangeAddressDestination

  @Serializable
  data object Offer : ChangeAddressDestination

  @Serializable
  data class AddressResult(
    val movingDate: LocalDate?,
  ) : ChangeAddressDestination
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
  val minDate: LocalDate,
  val maxDate: LocalDate,
)

@Serializable
internal data class SelectHousingMovingParameters(
  val isEligibleForStudent: Boolean,
  val maxNumberCoInsured: Int?,
  val maxSquareMeters: Int?,
  val extraBuildingTypes: List<ExtraBuildingType>,
  val moveFromAddressId: AddressId?,
  val minDate: LocalDate,
  val maxDate: LocalDate,
  val numberInsured: String,
  val moveIntentId: String,
)
