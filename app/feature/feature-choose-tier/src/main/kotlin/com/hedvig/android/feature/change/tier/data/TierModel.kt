package com.hedvig.android.feature.change.tier.data

import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.productvariant.ProductVariant
import kotlinx.datetime.LocalDate

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

data class ChangeTierDeductibleIntent(
  val activationDate: LocalDate,
  val quotes: List<TierDeductibleQuote>,
)

data class TierDeductibleQuote(
  val id: String,
  val tier: Tier,
  val deductible: Deductible,
  val premium: UiMoney,
  val displayItems: List<ChangeTierDeductibleDisplayItem>,
  val productVariant: ProductVariant,
)

data class ChangeTierDeductibleDisplayItem (
  val displayTitle: String,
  val displaySubtitle: String?,
  val displayValue: String,
)

