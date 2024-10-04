package com.hedvig.android.data.changetier.data

import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource.SELF_SERVICE
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource.TERMINATION_BETTER_COVERAGE
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource.TERMINATION_BETTER_PRICE
import com.hedvig.android.data.productvariant.ProductVariant
import kotlinx.datetime.LocalDate
import octopus.type.ChangeTierDeductibleSource

data class ChangeTierDeductibleIntent(
  val activationDate: LocalDate,
  val currentTierLevel: Int?,
  val currentTierName: String?,
  val quotes: List<TierDeductibleQuote>,
)

data class TierDeductibleQuote(
  val id: String,
  val tier: Tier,
  val deductible: Deductible?,
  val premium: UiMoney,
  val displayItems: List<ChangeTierDeductibleDisplayItem>,
  val productVariant: ProductVariant,
)

data class ChangeTierDeductibleDisplayItem(
  val displayTitle: String,
  val displaySubtitle: String?,
  val displayValue: String,
)

data class Tier(
  val tierName: String,
  val tierLevel: Int,
  val tierDisplayName: String?,
  val info: String?,
)

data class Deductible(
  val deductibleAmount: UiMoney?,
  val deductiblePercentage: Int?,
  val description: String,
) {
  val optionText = if (deductiblePercentage != null && deductibleAmount != null) {
    "$deductibleAmount + $deductiblePercentage%"
  } else if (deductiblePercentage != null) {
    "$deductiblePercentage%"
  } else {
    deductibleAmount?.toString() ?: ""
  }
}

enum class ChangeTierCreateSource {
  SELF_SERVICE,
  TERMINATION_BETTER_COVERAGE,
  TERMINATION_BETTER_PRICE,
}

internal fun ChangeTierCreateSource.toSource(): ChangeTierDeductibleSource {
  return when (this) {
    SELF_SERVICE -> ChangeTierDeductibleSource.SELF_SERVICE
    TERMINATION_BETTER_COVERAGE -> ChangeTierDeductibleSource.TERMINATION_BETTER_COVERAGE
    TERMINATION_BETTER_PRICE -> ChangeTierDeductibleSource.TERMINATION_BETTER_PRICE
  }
}
