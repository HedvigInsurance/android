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
  data class EnterNewAddress(
    val previousDestinationParameters: SelectHousingTypeParameters,
  ) : ChangeAddressDestination

  @Serializable
  data class EnterVillaInformation(
    val previousDestinationParameters: MovingParameters,
  ) : ChangeAddressDestination

  @Serializable
  data class Offer(
    val previousDestinationParameters: MovingParameters,
  ) : ChangeAddressDestination

  @Serializable
  data class AddressResult(
    val movingDate: LocalDate?,
  ) : ChangeAddressDestination
}

@Serializable
internal data class MovingParameters(
  val selectHousingTypeParameters: SelectHousingTypeParameters,
  val newAddressParameters: NewAddressParameters,
  val villaOnlyParameters: VillaOnlyParameters?,
)

@Serializable
internal data class VillaOnlyParameters(
  val yearOfConstruction: String?,
  val ancillaryArea: String?,
  val numberOfBathrooms: String?,
  val isSublet: Boolean,
  val extraBuildings: List<ExtraBuilding>,
)

@Serializable
internal data class SelectHousingTypeParameters(
  val isEligibleForStudent: Boolean,
  val maxNumberCoInsured: Int?,
  val maxSquareMeters: Int?,
  val extraBuildingTypes: List<ExtraBuildingType>,
  val moveFromAddressId: AddressId?,
  val minDate: LocalDate,
  val maxDate: LocalDate,
  val suggestedNumberInsured: String,
  val moveIntentId: String,
  val housingType: HousingType?,
)

@Serializable
internal data class NewAddressParameters(
  val street: String,
  val postalCode: String,
  val squareMeters: String,
  val isStudent: Boolean,
  val movingDate: LocalDate,
  val numberInsured: String,
)
