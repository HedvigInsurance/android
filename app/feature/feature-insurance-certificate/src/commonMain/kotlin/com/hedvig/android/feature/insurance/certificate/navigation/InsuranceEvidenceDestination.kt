package com.hedvig.android.feature.insurance.certificate.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
data object InsuranceEvidenceKey : HedvigNavKey

@Serializable
internal data class ShowCertificateKey(
  val certificateUrl: String,
) : HedvigNavKey
