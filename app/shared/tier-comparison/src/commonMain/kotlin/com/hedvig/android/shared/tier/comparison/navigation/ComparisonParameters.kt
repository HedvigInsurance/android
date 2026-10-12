package com.hedvig.android.shared.tier.comparison.navigation

import kotlinx.serialization.Serializable

@Serializable
data class ComparisonParameters(
  val termsIds: List<String>,
  val selectedTermsVersion: String?,
)
