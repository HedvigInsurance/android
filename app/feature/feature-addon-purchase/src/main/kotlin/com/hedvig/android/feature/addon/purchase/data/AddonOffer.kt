package com.hedvig.android.feature.addon.purchase.data

import arrow.core.NonEmptyList
import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.data.productvariant.AddonVariant
import com.hedvig.android.data.productvariant.ProductVariant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface GenerateAddonOfferResult {
  @Serializable
  data class AddonOfferDeflect(
    val pageTitle: String,
    val pageDescription: String,
    val type: AddonOfferDeflectType,
  ) : GenerateAddonOfferResult

  @Serializable
  data class AddonOfferResult(
    val pageTitle: String,
    val pageDescription: String,
    val umbrellaAddonQuote: UmbrellaAddonQuote,
    val currentTotalCost: ItemCost,
    val notificationMessage: String?,
    val contractId: String,
    val whatsIncludedPageTitle: String,
    val whatsIncludedPageDescription: String,
  ) : GenerateAddonOfferResult
}

internal enum class AddonOfferDeflectType {
  UPGRADE_TIER,
  GENERAL_CLOSE
}

/** Top-level addon type, like Travel+ or Car+ */
@Serializable
internal data class UmbrellaAddonQuote(
  val quoteId: String,
  val displayTitle: String,
  val displayDescription: String,
  val activationDate: LocalDate,
  val addonOffer: AddonOffer,
  val activeAddons: List<CurrentlyActiveAddon>,
  val baseInsuranceCost: ItemCost,
  val productVariant: ProductVariant,
)

internal sealed interface AddonOffer {
  data class Selectable(
    val fieldTitle: String,
    val selectionTitle: String,
    val selectionDescription: String,
    val addonOptions: NonEmptyList<AddonQuote>,
  ) : AddonOffer

  data class Toggleable(
    val addonOptions: NonEmptyList<AddonQuote>,
  ) : AddonOffer
}

/** Specific low-level addon, like Travel 45 Days or Car Drulle */
@Serializable
internal data class AddonQuote(
  val addonId: String,
  val displayTitle: String,
  val displayDescription: String,
  val addonVariant: AddonVariant,
  val displayDetails: List<Pair<String, String>>,
  val documents: List<TravelAddonQuoteInsuranceDocument>,
  val itemCost: ItemCost,
  val addonSubtype: String?,
)

@Serializable
internal data class TravelAddonQuoteInsuranceDocument(
  val displayName: String,
  val url: String,
)

@Serializable
internal data class CurrentlyActiveAddon(
  val displayTitle: String,
  val displayDescription: String?,
  val cost: ItemCost,
)
