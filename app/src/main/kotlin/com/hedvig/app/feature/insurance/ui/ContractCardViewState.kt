package com.hedvig.app.feature.insurance.ui

import com.hedvig.android.apollo.graphql.type.TypeOfContractGradientOption
import com.hedvig.app.util.apollo.ThemedIconUrls

data class ContractCardViewState(
  val id: String,
  val firstStatusPillText: String?,
  val secondStatusPillText: String?,
  val gradientOption: TypeOfContractGradientOption?,
  val displayName: String,
  val detailPills: List<String>,
  val logoUrls: ThemedIconUrls?,
)
