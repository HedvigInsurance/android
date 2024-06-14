package com.hedvig.android.feature.changeaddress.navigation

import com.hedvig.android.navigation.compose.Destination
import com.hedvig.android.navigation.compose.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

internal sealed interface ChangeAddressDestination {
  @Serializable
  data object SelectHousingType : ChangeAddressDestination, Destination

  @Serializable
  data object EnterNewAddress : ChangeAddressDestination, Destination

  @Serializable
  data object EnterVillaInformation : ChangeAddressDestination, Destination

  @Serializable
  data object Offer : ChangeAddressDestination, Destination

  @Serializable
  data class AddressResult(
    val movingDate: LocalDate?,
  ) : ChangeAddressDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<LocalDate?>())
    }
  }
}
