package com.hedvig.android.feature.changeaddress.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

internal sealed interface ChangeAddressDestination : Destination {
  @Serializable
  data object EnterNewAddress : ChangeAddressDestination

  @Serializable
  data object OfferDestination : ChangeAddressDestination

  @Serializable
  data object SelectHousingType : ChangeAddressDestination

  @Serializable
  data class AddressResult(
    val movingDate: String?,
  ) : ChangeAddressDestination
}
