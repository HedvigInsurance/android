package com.hedvig.android.feature.addon.purchase.data

import arrow.core.NonEmptyList
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.productvariant.InsuranceVariantDocument
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

internal sealed interface Addon {
  data class TravelAddonOffer(
    val addonOptions: NonEmptyList<TravelAddonQuote>,
    val title: String,
    val description: String,
    val activationDate: LocalDate,
  ) : Addon
}

@Serializable
internal data class TravelAddonQuote(
  val quoteId: String,
  val addonId: String,
  val displayName: String,
  val price: UiMoney,
  val addonVariant: AddonVariant,
)

@Serializable
internal data class AddonVariant(
  val documents: List<InsuranceVariantDocument>,
  val termsVersion: String,
)
