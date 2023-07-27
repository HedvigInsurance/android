package com.hedvig.android.feature.changeaddress.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

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
