package com.hedvig.android.feature.movingflow.data

import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.data.productvariant.toProductVariant
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.DisplayItem
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveHomeQuote.Deductible
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.MoveMtaQuote
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.feature.movingflow.fragment.MoveIntentQuotesFragment
import octopus.feature.movingflow.fragment.MoveQuoteDisplayItemFragment

@Serializable
internal data class MovingFlowQuotes(
  val homeQuotes: List<MoveHomeQuote>,
  val mtaQuotes: List<MoveMtaQuote>,
) {
  interface Quote {
    val premium: UiMoney
    val exposureName: String
    val productVariant: ProductVariant
    val startDate: LocalDate
    val displayItems: List<DisplayItem>
  }

  @Serializable
  data class MoveHomeQuote(
    val id: String,
    override val premium: UiMoney,
    override val startDate: LocalDate,
    override val displayItems: List<DisplayItem>,
    override val exposureName: String,
    override val productVariant: ProductVariant,
    val tierName: String,
    val tierLevel: Int,
    val tierDescription: String?,
    val deductible: Deductible?,
    val defaultChoice: Boolean,
    val relatedAddonQuotes: List<AddonQuote>,
  ) : Quote {
    val tierDisplayName = productVariant.displayTierName ?: tierName

    @Serializable
    data class Deductible(
      val amount: UiMoney,
      val percentage: Int?,
      val displayText: String,
    )
  }

  @Serializable
  data class MoveMtaQuote(
    override val premium: UiMoney,
    override val exposureName: String,
    override val productVariant: ProductVariant,
    override val startDate: LocalDate,
    override val displayItems: List<DisplayItem>,
  ) : Quote

  @Serializable
  data class AddonQuote(
    override val premium: UiMoney,
    override val startDate: LocalDate,
    override val displayItems: List<DisplayItem>,
    override val exposureName: String,
    override val productVariant: ProductVariant,
  ) : Quote

  @Serializable
  data class DisplayItem(
    val title: String,
    val subtitle: String?,
    val value: String,
  )
}

internal fun MoveIntentQuotesFragment.toMovingFlowQuotes(): MovingFlowQuotes {
  return MovingFlowQuotes(
    homeQuotes = (homeQuotes ?: emptyList()).map { houseQuote ->
      MoveHomeQuote(
        id = houseQuote.id,
        premium = UiMoney.fromMoneyFragment(houseQuote.premium),
        startDate = houseQuote.startDate,
        displayItems = houseQuote.displayItems.map { it.toDisplayItem() },
        exposureName = houseQuote.exposureName,
        productVariant = houseQuote.productVariant.toProductVariant(),
        tierName = houseQuote.tierName,
        tierLevel = houseQuote.tierLevel,
        tierDescription = houseQuote.productVariant.tierDescription,
        deductible = houseQuote.deductible?.let { deductible ->
          Deductible(
            amount = UiMoney.fromMoneyFragment(deductible.amount),
            percentage = deductible.percentage.takeIf { it != 0 },
            displayText = deductible.displayText,
          )
        },
        defaultChoice = houseQuote.defaultChoice,
        relatedAddonQuotes = emptyList(), // todo tier & addons: populate the related addons when backend returns them
      )
    },
    mtaQuotes = (mtaQuotes ?: emptyList()).map { mtaQuote ->
      MoveMtaQuote(
        premium = UiMoney.fromMoneyFragment(mtaQuote.premium),
        exposureName = mtaQuote.exposureName,
        productVariant = mtaQuote.productVariant.toProductVariant(),
        startDate = mtaQuote.startDate,
        displayItems = mtaQuote.displayItems.map { it.toDisplayItem() },
      )
    },
  )
}

private fun MoveQuoteDisplayItemFragment.toDisplayItem(): DisplayItem {
  return DisplayItem(
    title = displayTitle,
    subtitle = displaySubtitle,
    value = displayValue,
  )
}
