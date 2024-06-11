package com.hedvig.android.feature.changeaddress.navigation

import com.hedvig.android.navigation.compose.typeMapOfNullable
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

internal sealed interface ChangeAddressDestination {
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
  ) : ChangeAddressDestination {
    companion object {
      val typeMap = typeMapOfNullable<LocalDate>()
    }
  }
}
