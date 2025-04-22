package com.hedvig.android.feature.insurance.certificate.navigation


import com.hedvig.android.navigation.common.Destination
import kotlinx.serialization.Serializable

@Serializable
data object InsuranceEvidenceGraphDestination : Destination

internal sealed interface InsuranceEvidenceDestination {

  @Serializable
  data class InsuranceEvidenceEmailInput(
    val email: String,
  ) : InsuranceEvidenceDestination, Destination {
  }

  @Serializable
  data class ShowCertificate(
    val certificateUrl: String,
  ) : InsuranceEvidenceDestination, Destination
}
