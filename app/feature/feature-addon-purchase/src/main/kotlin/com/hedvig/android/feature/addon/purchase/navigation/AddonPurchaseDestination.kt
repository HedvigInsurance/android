package com.hedvig.android.feature.addon.purchase.navigation

import com.hedvig.android.data.addons.data.TravelAddonBannerSource
import com.hedvig.android.feature.addon.purchase.data.CurrentTravelAddon
import com.hedvig.android.feature.addon.purchase.data.TravelAddonQuote
import com.hedvig.android.feature.addon.purchase.navigation.AddonPurchaseDestination.TravelInsurancePlusExplanation.TravelPerilData
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class AddonPurchaseGraphDestination(
  val insuranceIds: List<String>,
  val source: TravelAddonBannerSource,
) : Destination {
  companion object : DestinationNavTypeAware {
    override val typeList: List<KType> = listOf(typeOf<TravelAddonBannerSource>())
  }
}

internal sealed interface AddonPurchaseDestination {
  @Serializable
  data object ChooseInsuranceToAddAddonDestination : AddonPurchaseDestination, Destination

  @Serializable
  data class CustomizeAddon(val insuranceId: String) : AddonPurchaseDestination, Destination

  @Serializable
  data class TravelInsurancePlusExplanation(
    val perilData: List<TravelPerilData>,
  ) : AddonPurchaseDestination, Destination {
    @Serializable
    data class TravelPerilData(
      val title: String,
      val description: String?,
      val covered: List<String>,
      val colorCode: String?,
      val isEnabled: Boolean = true,
    )

    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<TravelPerilData>>())
    }
  }

  @Serializable
  data class Summary(
    val params: SummaryParameters,
  ) : AddonPurchaseDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SummaryParameters>())
    }
  }

  @Serializable
  data class SubmitSuccess(val activationDate: LocalDate) : AddonPurchaseDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<LocalDate>(),
      )
    }
  }

  @Serializable
  data object SubmitFailure : AddonPurchaseDestination, Destination

  @Serializable
  data object TravelAddonTriage : AddonPurchaseDestination, Destination
}

@Serializable
internal data class SummaryParameters(
  val offerDisplayName: String,
  val quote: TravelAddonQuote,
  val activationDate: LocalDate,
  val currentTravelAddon: CurrentTravelAddon?,
)
