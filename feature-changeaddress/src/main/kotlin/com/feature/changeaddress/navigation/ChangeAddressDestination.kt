package com.feature.changeaddress.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

internal sealed interface Destinations : Destination {
  @Serializable
  object ChangeAddress : Destinations
}

internal sealed interface ChangeAddressDestination : Destination {
  @Serializable
  object EnterNewAddress : ChangeAddressDestination

  @Serializable
  object OfferDestination : ChangeAddressDestination

  @Serializable
  object SelectHousingType : ChangeAddressDestination

  @Serializable
  object AddressResult : ChangeAddressDestination
}
