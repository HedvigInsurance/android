package com.hedvig.app.feature.insurance.ui

import com.hedvig.android.core.common.android.ThemedIconUrls
import com.hedvig.android.core.ui.insurance.GradientType

data class ContractCardViewState(
  val id: String,
  val firstStatusPillText: String?,
  val secondStatusPillText: String?,
  val gradientType: GradientType,
  val displayName: String,
  val detailPills: List<String>,
  val logoUrls: ThemedIconUrls?,
)
