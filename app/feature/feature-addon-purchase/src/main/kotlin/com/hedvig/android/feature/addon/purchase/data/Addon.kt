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
  val addonId: String,
  val displayName: String,
  val price: UiMoney,
  val addonVariant: AddonVariant,
  val displayDetails: List<Pair<String, String>>,
)

@Serializable
internal data class CurrentTravelAddon(
  val price: UiMoney,
  val displayDetails: List<Pair<String, String>>,
)
