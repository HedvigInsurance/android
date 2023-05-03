package com.example.feature.changeaddress.navigation

import com.example.feature.changeaddress.data.MoveQuote
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
  data class MoveQuotes(
    val quotes: List<MoveQuote>,
  ) : ChangeAddressDestination

  @Serializable
  object AddressResult : ChangeAddressDestination
}

