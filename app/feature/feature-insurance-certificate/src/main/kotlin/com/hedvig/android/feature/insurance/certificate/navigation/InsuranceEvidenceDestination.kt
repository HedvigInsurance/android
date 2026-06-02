package com.hedvig.android.feature.insurance.certificate.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
data object InsuranceEvidenceGraphDestination : HedvigNavKey

internal sealed interface InsuranceEvidenceDestination {
  @Serializable
  data class ShowCertificate(
    val certificateUrl: String,
  ) : InsuranceEvidenceDestination, HedvigNavKey
}
