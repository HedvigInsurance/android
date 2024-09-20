package com.hedvig.android.feature.change.tier.data

import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.design.system.hedvig.DropdownItem.SimpleDropdownItem
import octopus.fragment.MoneyFragment

data class CustomizeContractData(
  val contractGroup: ContractGroup,
  val displayName: String,
  val displaySubtitle: String,
  val tierDropdownData: List<SimpleDropdownItem>,
  val deductibleDropdownData: List<SimpleDropdownItem>,
  val currentQuoteId: Int, // todo: maybe?
)

data class TierQuote(
  val deductibleAmount: MoneyFragment,
  val deductiblePercentage: Int,
  val tierName: String,
  val tierLevel: Int,
  val tierPremium: MoneyFragment,
  val id: Int, // todo: maybe?
)
