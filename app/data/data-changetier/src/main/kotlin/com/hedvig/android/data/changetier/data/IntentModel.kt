package com.hedvig.android.data.changetier.data

import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource.SELF_SERVICE
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource.TERMINATION_BETTER_COVERAGE
import com.hedvig.android.data.changetier.data.ChangeTierCreateSource.TERMINATION_BETTER_PRICE
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.ProductVariant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.type.ChangeTierDeductibleSource

data class ChangeTierDeductibleIntent(
  val activationDate: LocalDate,
  val quotes: List<TierDeductibleQuote>,
)

@Serializable
data class TierDeductibleQuote(
  val id: String,
  val tier: Tier,
  val deductible: Deductible?,
  val premium: UiMoney,
  val displayItems: List<ChangeTierDeductibleDisplayItem>,
  val productVariant: ProductVariant,
  val addons: List<ChangeTierDeductibleAddonQuote>,
  val currentTotalCost: TotalCost,
  val newTotalCost: TotalCost,
  val costBreakdown: List<Pair<String, String>>,
)

@Serializable
data class TotalCost(
  val monthlyGross: UiMoney,
  val monthlyNet: UiMoney,
)

@Serializable
data class ChangeTierDeductibleDisplayItem(
  val displayTitle: String,
  val displaySubtitle: String?,
  val displayValue: String,
)

@Serializable
data class ChangeTierDeductibleAddonQuote(
  val addonVariant: AddonVariant,
)

@Serializable
data class Tier(
  val tierName: String,
  val tierLevel: Int,
  val tierDisplayName: String?,
  val tierDescription: String?,
)

@Serializable
data class Deductible(
  val deductibleAmount: UiMoney?,
  val deductiblePercentage: Int?,
  val description: String,
) {
  private val percentageNotZero = deductiblePercentage != null && deductiblePercentage != 0
  val optionText = if (percentageNotZero && deductibleAmount != null) {
    "$deductibleAmount + $deductiblePercentage%"
  } else if (percentageNotZero) {
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
