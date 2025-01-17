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
import com.hedvig.android.feature.movingflow.data.MovingFlowQuotes.UserExcludable.ExclusionDialogInfo
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
  internal data class MoveHomeQuote(
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
    val relatedAddonQuotes: List<HomeAddonQuote>,
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
    override val exposureName: String,
    override val productVariant: ProductVariant,
    override val startDate: LocalDate,
    override val displayItems: List<DisplayItem>,
    val relatedAddonQuotes: List<MtaAddonQuote>,
  ) : Quote

  @Serializable
  internal data class DisplayItem(
    val title: String,
    val subtitle: String?,
    val value: String,
  )

  internal sealed interface AddonQuote {
    val addonId: AddonId
    val premium: UiMoney
    val startDate: LocalDate
    val displayItems: List<DisplayItem>
    val exposureName: String
    val addonVariant: AddonVariant

    @Serializable
    data class HomeAddonQuote(
      override val addonId: AddonId,
      override val premium: UiMoney,
      override val startDate: LocalDate,
      override val displayItems: List<DisplayItem>,
      override val exposureName: String,
      override val addonVariant: AddonVariant,
      override val isExcludedByUser: Boolean,
      override val exclusionDialogInfo: ExclusionDialogInfo?,
    ) : AddonQuote, UserExcludable

    @Serializable
    data class MtaAddonQuote(
      override val addonId: AddonId,
      override val premium: UiMoney,
      override val startDate: LocalDate,
      override val displayItems: List<DisplayItem>,
      override val exposureName: String,
      override val addonVariant: AddonVariant,
    ) : AddonQuote
  }

  internal interface UserExcludable {
    val isExcludedByUser: Boolean
    val exclusionDialogInfo: ExclusionDialogInfo?

    @Serializable
    data class ExclusionDialogInfo(
      val addonId: AddonId,
      val title: String,
      val description: String,
      val confirmButtonTitle: String,
      val cancelButtonTitle: String,
    )
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
        relatedAddonQuotes = houseQuote.addons.orEmpty().map { addon ->
          HomeAddonQuote(
            addonId = AddonId(addon.addonId),
            premium = UiMoney.fromMoneyFragment(addon.premium),
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
            exclusionDialogInfo = addon.removeDialogInfo?.let { removeDialogInfo ->
              ExclusionDialogInfo(
                addonId = AddonId(addon.addonId),
                title = removeDialogInfo.title,
                description = removeDialogInfo.description,
                confirmButtonTitle = removeDialogInfo.confirmButtonTitle,
                cancelButtonTitle = removeDialogInfo.cancelButtonTitle,
              )
            },
            isExcludedByUser = false,
          )
        },
      )
    },
    mtaQuotes = mtaQuotes.orEmpty().map { mtaQuote ->
      MoveMtaQuote(
        premium = UiMoney.fromMoneyFragment(mtaQuote.premium),
        exposureName = mtaQuote.exposureName,
        productVariant = mtaQuote.productVariant.toProductVariant(),
        startDate = mtaQuote.startDate,
        displayItems = mtaQuote.displayItems.map { it.toDisplayItem() },
        relatedAddonQuotes = mtaQuote.addons.orEmpty().map { addon ->
          MtaAddonQuote(
            addonId = AddonId(addon.addonId),
            premium = UiMoney.fromMoneyFragment(addon.premium),
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
