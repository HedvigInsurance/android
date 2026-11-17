package com.hedvig.android.feature.travelcertificate.navigation

import com.hedvig.android.feature.travelcertificate.data.TravelCertificateTravellersStep
import com.hedvig.android.feature.travelcertificate.data.TravelCertificateUrl
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
internal data object TravelCertificateChooseContractKey : HedvigNavKey

@Serializable
internal data class TravelCertificateDateInputKey(
  val contractId: String?,
) : HedvigNavKey

@Serializable
internal data class TravelCertificateTravellersInputKey(
  val primaryInput: TravelCertificateTravellersStep,
) : HedvigNavKey

@Serializable
internal data class ShowCertificateKey(
  val travelCertificateUrl: TravelCertificateUrl,
) : HedvigNavKey
