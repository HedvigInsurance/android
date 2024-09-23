package com.hedvig.android.feature.change.tier.data

import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem

data class CustomizeContractData(
  val contractGroup: ContractGroup,
  val displayName: String,
  val displaySubtitle: String,
  val tierDropdownData: List<SimpleDropdownItem>,
  val deductibleDropdownData: List<SimpleDropdownItem>,
  val currentDisplayPremium: String,
)

data class TierQuote(
  val deductibleAmount: UiMoney,
  val deductiblePercentage: Int,
  val tierName: String,
  val tierLevel: Int,
  val displayPremium: String,
  val id: Int, // todo: maybe?
)
