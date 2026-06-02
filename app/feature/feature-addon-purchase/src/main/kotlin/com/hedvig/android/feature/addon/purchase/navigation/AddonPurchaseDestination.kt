package com.hedvig.android.feature.addon.purchase.navigation

import com.hedvig.android.core.uidata.ItemCost
import com.hedvig.android.data.addons.data.AddonBannerSource
import com.hedvig.android.data.contract.ContractId
import com.hedvig.android.data.productvariant.ProductVariant
import com.hedvig.android.feature.addon.purchase.data.AddonQuote
import com.hedvig.android.feature.addon.purchase.data.CurrentlyActiveAddon
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.NavKeyTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.type.AvailableAddon

@Serializable
data class AddonPurchaseGraphDestination(
  val insuranceIds: List<String> = emptyList(),
  val preselectedAddonDisplayName: String? = null,
  val source: AddonBannerSource = AddonBannerSource.TRAVEL_DEEPLINK,
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(typeOf<AddonBannerSource>())
  }
}

/**
 * Deep-link entry for the travel/car addon flow. The URI → key mapping (path-derived [source] and
 * the optional `contractId` query param) is resolved by the centralized deep-link matcher in `:app`.
 */
@Serializable
data class TravelAddonTriage(
  val source: AddonBannerSource,
  val contractId: String?,
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(typeOf<AddonBannerSource>())
  }
}

internal sealed interface AddonPurchaseDestination {
  @Serializable
  data object ChooseInsuranceToAddAddonDestination : AddonPurchaseDestination, HedvigNavKey

  @Serializable
  data class CustomizeAddon(
    val insuranceId: String,
    val preselectedAddonDisplayNames: List<String>,
  ) : AddonPurchaseDestination, HedvigNavKey

  @Serializable
  data class TravelInsurancePlusExplanation(
    val perilData: PerilComparisonParams,
  ) : AddonPurchaseDestination, HedvigNavKey {
    @Serializable
    data class TravelPerilData(
      val title: String,
      val description: String?,
      val covered: List<String>,
      val colorCode: String?,
      val isEnabled: Boolean = true,
    )

    companion object : NavKeyTypeAware {
      override val typeList: List<KType> = listOf(typeOf<PerilComparisonParams>())
    }
  }

  @Serializable
  data class Summary(
    val params: SummaryParameters,
  ) : AddonPurchaseDestination, HedvigNavKey {
    companion object : NavKeyTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SummaryParameters>())
    }
  }

  @Serializable
  data class SubmitSuccess(val activationDate: LocalDate) : AddonPurchaseDestination, HedvigNavKey {
    companion object : NavKeyTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<LocalDate>(),
      )
    }
  }

  @Serializable
  data object SubmitFailure : AddonPurchaseDestination, HedvigNavKey
}

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
