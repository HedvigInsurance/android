package com.hedvig.android.feature.changeaddress.navigation

import com.hedvig.android.feature.changeaddress.data.AddressId
import com.hedvig.android.feature.changeaddress.data.ExtraBuilding
import com.hedvig.android.feature.changeaddress.data.ExtraBuildingType
import com.hedvig.android.feature.changeaddress.data.HousingType
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data object ChangeAddressGraphDestination : Destination

internal sealed interface ChangeAddressDestination {
  @Serializable
  data object SelectHousingType : ChangeAddressDestination, Destination

  @Serializable
  data class EnterNewAddress(
    val previousDestinationParameters: SelectHousingTypeParameters,
  ) : ChangeAddressDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SelectHousingTypeParameters>())
    }
  }

  @Serializable
  data class EnterVillaInformation(
    val previousDestinationParameters: MovingParameters,
  ) : ChangeAddressDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<MovingParameters>())
    }
  }

  @Serializable
  data class Offer(
    val previousDestinationParameters: MovingParameters,
  ) : ChangeAddressDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<MovingParameters>())
    }
  }

  @Serializable
  data class AddressResult(
    val movingDate: LocalDate?,
  ) : ChangeAddressDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<LocalDate?>())
    }
  }
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
