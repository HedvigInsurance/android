package com.hedvig.android.feature.travelcertificate.navigation

import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.NavKeyTypeAware
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data object TravelCertificateGraphDestination : HedvigNavKey

internal sealed interface TravelCertificateDestination {
  @Serializable
  data object TravelCertificateChooseContract : TravelCertificateDestination, HedvigNavKey

  @Serializable
  data class TravelCertificateDateInput(
    val contractId: String?,
  ) : TravelCertificateDestination, HedvigNavKey

  @Serializable
  data class TravelCertificateTravellersInput(
    val primaryInput: TravelCertificatePrimaryInput,
  ) : TravelCertificateDestination, HedvigNavKey {
    @Serializable
    data class TravelCertificatePrimaryInput(
      val email: String,
      val travelDate: LocalDate,
      val contractId: String,
    )

    companion object : NavKeyTypeAware {
      override val typeList: List<KType> = listOf(typeOf<TravelCertificatePrimaryInput>())
    }
  }

  @Serializable
  data class ShowCertificate(
    val travelCertificateUrl: TravelCertificateUrl,
  ) : TravelCertificateDestination, HedvigNavKey {
    companion object : NavKeyTypeAware {
      override val typeList: List<KType> = listOf(typeOf<TravelCertificateUrl>())
    }
  }
}

val travelCertificateCrossSellBottomSheetPermittingDestinations: List<KClass<out HedvigNavKey>> = listOf(
  TravelCertificateGraphDestination::class,
)
