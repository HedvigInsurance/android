package com.hedvig.android.feature.addon.purchase.data

import arrow.core.NonEmptyList
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.AddonVariant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

internal sealed interface Addon {
  data class TravelAddonOffer(
    val addonOptions: NonEmptyList<TravelAddonQuote>,
    val title: String,
    val description: String,
    val activationDate: LocalDate,
    val currentTravelAddon: CurrentTravelAddon?,
  ) : Addon
}

@Serializable
internal data class TravelAddonQuote(
  val quoteId: String,
  val addonSubtype: String,
  val addonId: String,
  val displayName: String,
  val addonVariant: AddonVariant,
  val displayDetails: List<Pair<String, String>>,
  val documents: List<TravelAddonQuoteInsuranceDocument>,
  val displayNameLong: String,
  val itemCost: ItemCost,
)

@Serializable
internal data class TravelAddonQuoteInsuranceDocument(
  val displayName: String,
  val url: String,
)

@Serializable
internal data class CurrentTravelAddon(
  val displayDetails: List<Pair<String, String>>,
  val displayNameLong: String,
  val netPremium: UiMoney,
)

@Serializable
data class ItemCost(
  val monthlyNet: UiMoney,
  val monthlyGross: UiMoney,
  val discounts: List<ItemCostDiscount>,
)

@Serializable
data class ItemCostDiscount(
  val campaignCode: String,
  val displayName: String,
  val displayValue: String,
  val explanation: String,
)
