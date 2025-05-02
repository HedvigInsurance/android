package com.hedvig.android.feature.insurance.certificate.navigation

import com.hedvig.android.navigation.common.Destination
import kotlinx.serialization.Serializable

@Serializable
data object InsuranceEvidenceGraphDestination : Destination

internal sealed interface InsuranceEvidenceDestination {
  @Serializable
  data object InsuranceEvidenceEmailInput : InsuranceEvidenceDestination, Destination

  @Serializable
  data class ShowCertificate(
    val certificateUrl: String,
  ) : InsuranceEvidenceDestination, Destination
}
