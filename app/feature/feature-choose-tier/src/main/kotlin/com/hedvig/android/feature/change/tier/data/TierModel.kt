package com.hedvig.android.feature.change.tier.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hedvig.android.data.contract.ContractGroup
import hedvig.resources.R

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
) {
  val optionText = if (deductiblePercentage !=
    null
  ) {
    "$deductibleAmount + $deductiblePercentage"
  } else {
    deductibleAmount ?: ""
  }
}

@Composable
fun Deductible.description() = if (deductiblePercentage != null &&
  deductibleAmount != null
) {
  stringResource(R.string.TIER_FLOW_DEDUCTIBLE_FIXED_AND_PERCENTAGE, deductiblePercentage)
} else if (deductibleAmount == null &&
  deductiblePercentage != null
) {
  stringResource(R.string.TIER_FLOW_DEDUCTIBLE_PERCENTAGE_ONLY, deductiblePercentage)
} else {
  null
}
