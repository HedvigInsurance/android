package com.hedvig.android.feature.travelcertificate.navigation

import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.feature.travelcertificate.navigation.TravelCertificateDestination.TravelCertificateTravellersInput.TravelCertificatePrimaryInput
import com.hedvig.android.navigation.compose.typeMapOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

internal sealed interface TravelCertificateDestination {
  @Serializable
  data object TravelCertificateHistory : TravelCertificateDestination

  @Serializable
  data object TravelCertificateChooseContract : TravelCertificateDestination

  @Serializable
  data class TravelCertificateDateInput(
    val contractId: String?,
  ) : TravelCertificateDestination

  @Serializable
  data class TravelCertificateTravellersInput(
    val primaryInput: TravelCertificatePrimaryInput,
  ) : TravelCertificateDestination {
    @Serializable
    data class TravelCertificatePrimaryInput(
      val email: String,
      val travelDate: LocalDate,
      val contractId: String,
    )

    companion object {
      val typeMap = typeMapOf<TravelCertificatePrimaryInput>()
    }
  }

  @Serializable
  data class ShowCertificate(
    val travelCertificateUrl: TravelCertificateUrl,
  ) : TravelCertificateDestination {
    companion object {
      val typeMap = typeMapOf<TravelCertificateUrl>()
    }
  }
}
