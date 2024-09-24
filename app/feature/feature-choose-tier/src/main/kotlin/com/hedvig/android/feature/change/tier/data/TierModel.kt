package com.hedvig.android.feature.change.tier.data

import com.hedvig.android.data.contract.ContractGroup

data class CustomizeContractData(
  val contractGroup: ContractGroup,
  val displayName: String,
  val displaySubtitle: String,
  val tierData: List<Tier>,
  val deductibleData: List<Deductible>,
  val currentDisplayPremium: String,
)

data class Tier(
  val tierName: String,
  val tierLevel: Int,
  val displayPremium: String,
  val info: String,
)

data class Deductible(
  val deductibleAmount: String?,
  val deductiblePercentage: String?,
  val displayPremium: String,
  val description: String,
) {
  val optionText = if (deductiblePercentage !=
    null
  ) {
    "$deductibleAmount + $deductiblePercentage"
  } else {
    deductibleAmount ?: ""
  }
}
