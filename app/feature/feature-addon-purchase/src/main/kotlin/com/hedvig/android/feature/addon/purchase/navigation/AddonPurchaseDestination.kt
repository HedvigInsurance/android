package com.hedvig.android.feature.addon.purchase.navigation

import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.addon.purchase.data.AddonQuote
import com.hedvig.android.feature.addon.purchase.data.CurrentlyActiveAddon
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class AddonPurchaseKey(
  val insuranceIds: List<String> = emptyList(),
  val preselectedAddonDisplayName: String? = null,
  val source: AddonBannerSource = AddonBannerSource.TRAVEL_DEEPLINK,
) : HedvigNavKey

/**
 * Deep-link entry for the travel/car addon flow. The URI → key mapping (path-derived [source] and
 * the optional `contractId` query param) is resolved by the centralized deep-link matcher in `:app`.
 */
@Serializable
data class TravelAddonTriageKey(
  val source: AddonBannerSource,
  val contractId: String?,
) : HedvigNavKey

@Serializable
internal data class CustomizeAddonKey(
  val insuranceId: String,
  val preselectedAddonDisplayNames: List<String>,
) : HedvigNavKey

@Serializable
internal data class TravelInsurancePlusExplanationKey(
  val perilData: PerilComparisonParams,
) : HedvigNavKey {
  @Serializable
  data class TravelPerilData(
    val title: String,
    val description: String?,
    val covered: List<String>,
    val colorCode: String?,
    val isEnabled: Boolean = true,
  )
}

@Serializable
internal data class SummaryKey(
  val params: SummaryParameters,
) : HedvigNavKey

@Serializable
internal data class SubmitSuccessKey(val activationDate: LocalDate) : HedvigNavKey

@Serializable
internal data object SubmitFailureKey : HedvigNavKey

@Serializable
internal data class SummaryParameters(
  val productVariant: ProductVariant,
  val contractId: String,
  val baseInsuranceCost: ItemCost,
  val chosenQuotes: List<AddonQuote>,
  val activationDate: LocalDate,
  val currentlyActiveAddons: List<CurrentlyActiveAddon>,
  val quoteId: String,
  val notificationMessage: String?,
  val addonType: AddonType,
)

@Serializable
internal enum class AddonType {
  SELECTABLE,
  TOGGLEABLE,
}
