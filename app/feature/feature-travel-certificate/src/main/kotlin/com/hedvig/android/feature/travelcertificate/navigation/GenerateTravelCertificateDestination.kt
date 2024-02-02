package com.hedvig.android.feature.travelcertificate.navigation

import com.hedvig.android.feature.travelcertificate.CoInsured
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.kiwi.navigationcompose.typed.Destination
import kotlinx.serialization.Serializable

internal sealed interface GenerateTravelCertificateDestination : Destination {
  @Serializable
  data object TravelCertificateInformation : GenerateTravelCertificateDestination

  @Serializable
  data object TravelCertificateInput : GenerateTravelCertificateDestination

  @Serializable
  data class AddCoInsured(
    val coInsured: CoInsured?,
  ) : GenerateTravelCertificateDestination

  @Serializable
  data class ShowCertificate(
    val travelCertificateUrl: TravelCertificateUrl,
  ) : GenerateTravelCertificateDestination
}
