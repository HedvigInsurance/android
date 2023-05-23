package com.hedvig.android.feature.travelcertificate.navigation

import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

internal sealed interface Destinations : Destination {
  @Serializable
  object GenerateTravelCertificate : Destinations
}

internal sealed interface GenerateTravelCertificateDestination : Destination {

  @Serializable
  data class TravelCertificateInput(
    val email: String?
  ) : GenerateTravelCertificateDestination

  @Serializable
  data class AddCoInsured(
    val id: String?
  ) : GenerateTravelCertificateDestination
}
