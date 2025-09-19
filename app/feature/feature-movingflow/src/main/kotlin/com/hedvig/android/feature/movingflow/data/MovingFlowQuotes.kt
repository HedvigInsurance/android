package com.hedvig.android.feature.movingflow.data

import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.data.productvariant.toAddonVariant
import com.hedvig.android.data.productvariant.toProductVariant
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.AddonQuote.HomeAddonQuote
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.AddonQuote.MtaAddonQuote
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
    val previousPremium: UiMoney?
    val discounts: List<ContractDiscount>
    val exposureName: String
    val productVariant: ProductVariant
    val startDate: LocalDate
    val displayItems: List<DisplayItem>
    val relatedAddonQuotes: List<AddonQuote>

    val includedRelatedAddonQuotes
      get() = relatedAddonQuotes.filter { addonQuote ->
        when (addonQuote) {
          is HomeAddonQuote -> !addonQuote.isExcludedByUser
          is MtaAddonQuote -> true
        }
      }
  }

  @Serializable
  internal data class MoveHomeQuote(
    val id: String,
    override val premium: UiMoney,
    override val previousPremium: UiMoney?,
    override val startDate: LocalDate,
    override val discounts: List<ContractDiscount>,
    override val displayItems: List<DisplayItem>,
    override val exposureName: String,
    override val productVariant: ProductVariant,
    override val relatedAddonQuotes: List<HomeAddonQuote>,
    val tierName: String,
    val tierLevel: Int,
    val tierDescription: String?,
    val deductible: Deductible?,
    val defaultChoice: Boolean,
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
  internal data class MoveMtaQuote(
    override val premium: UiMoney,
    override val previousPremium: UiMoney?,
    override val exposureName: String,
    override val productVariant: ProductVariant,
    override val startDate: LocalDate,
    override val discounts: List<ContractDiscount>,
    override val displayItems: List<DisplayItem>,
    override val relatedAddonQuotes: List<MtaAddonQuote>,
  ) : Quote

  @Serializable
  internal data class DisplayItem(
    val title: String,
    val subtitle: String?,
    val value: String,
  )

  @Serializable
  internal data class ContractDiscount(
    val displayName: String,
    val discountValue: String,
  )

  internal sealed interface AddonQuote {
    val addonId: AddonId
    val premium: UiMoney
    val previousPremium: UiMoney?
    val discounts: List<ContractDiscount>
    val startDate: LocalDate
    val displayItems: List<DisplayItem>
    val exposureName: String
    val addonVariant: AddonVariant
    val coverageDisplayName: String

    @Serializable
    data class HomeAddonQuote(
      val relatedQuoteId: String,
      override val addonId: AddonId,
      override val premium: UiMoney,
      override val previousPremium: UiMoney?,
      override val discounts: List<ContractDiscount>,
      override val startDate: LocalDate,
      override val displayItems: List<DisplayItem>,
      override val exposureName: String,
      override val addonVariant: AddonVariant,
      override val isExcludedByUser: Boolean,
      override val coverageDisplayName: String,
    ) : AddonQuote, UserExcludable

    @Serializable
    data class MtaAddonQuote(
      override val addonId: AddonId,
      override val premium: UiMoney,
      override val previousPremium: UiMoney?,
      override val discounts: List<ContractDiscount>,
      override val startDate: LocalDate,
      override val displayItems: List<DisplayItem>,
      override val exposureName: String,
      override val addonVariant: AddonVariant,
      override val coverageDisplayName: String,
    ) : AddonQuote
  }

  internal interface UserExcludable {
    val isExcludedByUser: Boolean
  }
}

@Serializable
@JvmInline
internal value class AddonId(val id: String)

internal fun MoveIntentQuotesFragment.toMovingFlowQuotes(): MovingFlowQuotes {
  return MovingFlowQuotes(
    homeQuotes = homeQuotes.orEmpty().map { houseQuote ->
      MoveHomeQuote(
        id = houseQuote.id,
        premium = UiMoney.fromMoneyFragment(houseQuote.cost.monthlyNet),
        previousPremium = UiMoney.fromMoneyFragment(houseQuote.cost.monthlyGross).takeIf {
          houseQuote.cost.monthlyGross.amount != houseQuote.cost.monthlyNet.amount
        },
        startDate = houseQuote.startDate,
        discounts = houseQuote.cost.discounts.map { discount ->
          MovingFlowQuotes.ContractDiscount(
            displayName = discount.displayName,
            discountValue = discount.displayValue,
          )
        },
        displayItems = houseQuote.displayItems.map { it.toDisplayItem() },
        exposureName = houseQuote.exposureName,
        productVariant = houseQuote.productVariant.toProductVariant(),
        relatedAddonQuotes = houseQuote.addons.orEmpty().map { addon ->
          HomeAddonQuote(
            relatedQuoteId = houseQuote.id,
            addonId = AddonId(addon.addonId),
            premium = UiMoney.fromMoneyFragment(addon.cost.monthlyNet),
            previousPremium = UiMoney.fromMoneyFragment(addon.cost.monthlyGross).takeIf {
              addon.cost.monthlyGross.amount != addon.cost.monthlyNet.amount
            },
            discounts = addon.cost.discounts.map { discount ->
              MovingFlowQuotes.ContractDiscount(
                displayName = discount.displayName,
                discountValue = discount.displayValue,
              )
            },
            startDate = addon.startDate,
            exposureName = addon.displayName,
            displayItems = addon.displayItems.map {
              DisplayItem(
                title = it.displayTitle,
                subtitle = it.displaySubtitle,
                value = it.displayValue,
              )
            },
            addonVariant = addon.addonVariant.toAddonVariant(),
            isExcludedByUser = false,
            coverageDisplayName = addon.coverageDisplayName,
          )
        },
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
      )
    },
    mtaQuotes = mtaQuotes.orEmpty().map { mtaQuote ->
      MoveMtaQuote(
        premium = UiMoney.fromMoneyFragment(mtaQuote.cost.monthlyNet),
        previousPremium = UiMoney.fromMoneyFragment(mtaQuote.cost.monthlyGross).takeIf {
          mtaQuote.cost.monthlyGross.amount != mtaQuote.cost.monthlyNet.amount
        },
        discounts = mtaQuote.cost.discounts.map { discount ->
          MovingFlowQuotes.ContractDiscount(
            displayName = discount.displayName,
            discountValue = discount.displayValue,
          )
        },
        exposureName = mtaQuote.exposureName,
        productVariant = mtaQuote.productVariant.toProductVariant(),
        startDate = mtaQuote.startDate,
        displayItems = mtaQuote.displayItems.map { it.toDisplayItem() },
        relatedAddonQuotes = mtaQuote.addons.orEmpty().map { addon ->
          MtaAddonQuote(
            addonId = AddonId(addon.addonId),
            premium = UiMoney.fromMoneyFragment(addon.cost.monthlyNet),
            previousPremium = UiMoney.fromMoneyFragment(addon.cost.monthlyGross).takeIf {
              addon.cost.monthlyGross.amount != addon.cost.monthlyNet.amount
            },
            discounts = addon.cost.discounts.map { discount ->
              MovingFlowQuotes.ContractDiscount(
                displayName = discount.displayName,
                discountValue = discount.displayValue,
              )
            },
            startDate = addon.startDate,
            exposureName = addon.displayName,
            displayItems = addon.displayItems.map {
              DisplayItem(
                title = it.displayTitle,
                subtitle = it.displaySubtitle,
                value = it.displayValue,
              )
            },
            addonVariant = addon.addonVariant.toAddonVariant(),
            coverageDisplayName = addon.coverageDisplayName,
          )
        },
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
