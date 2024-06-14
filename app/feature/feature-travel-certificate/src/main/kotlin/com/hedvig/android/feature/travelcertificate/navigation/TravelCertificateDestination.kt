package com.hedvig.android.feature.travelcertificate.navigation

import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.navigation.compose.Destination
import com.hedvig.android.navigation.compose.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

internal sealed interface TravelCertificateDestination {
  @Serializable
  data object TravelCertificateHistory : TravelCertificateDestination, Destination

  @Serializable
  data object TravelCertificateChooseContract : TravelCertificateDestination, Destination

  @Serializable
  data class TravelCertificateDateInput(
    val contractId: String?,
  ) : TravelCertificateDestination, Destination

  @Serializable
  data class TravelCertificateTravellersInput(
    val primaryInput: TravelCertificatePrimaryInput,
  ) : TravelCertificateDestination, Destination {
    @Serializable
    data class TravelCertificatePrimaryInput(
      val email: String,
      val travelDate: LocalDate,
      val contractId: String,
    )

    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<TravelCertificatePrimaryInput>())
    }
  }

  @Serializable
  data class ShowCertificate(
    val travelCertificateUrl: TravelCertificateUrl,
  ) : TravelCertificateDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<TravelCertificateUrl>())
    }
  }
}
