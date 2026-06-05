package com.hedvig.android.feature.travelcertificate.navigation

import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.navigation.common.CrossSellEligibleDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data object TravelCertificateKey : HedvigNavKey, CrossSellEligibleDestination

@Serializable
internal data object TravelCertificateChooseContractKey : HedvigNavKey

@Serializable
internal data class TravelCertificateDateInputKey(
  val contractId: String?,
) : HedvigNavKey

@Serializable
internal data class TravelCertificateTravellersInputKey(
  val primaryInput: TravelCertificatePrimaryInput,
) : HedvigNavKey {
  @Serializable
  data class TravelCertificatePrimaryInput(
    val email: String,
    val travelDate: LocalDate,
    val contractId: String,
  )
}

@Serializable
internal data class ShowCertificateKey(
  val travelCertificateUrl: TravelCertificateUrl,
) : HedvigNavKey
