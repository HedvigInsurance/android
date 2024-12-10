package com.hedvig.android.feature.addon.purchase.navigation

import com.hedvig.android.feature.addon.purchase.data.TravelAddonQuote
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class AddonPurchaseGraphDestination(val insuranceIds: List<String>) : Destination

internal sealed interface AddonPurchaseDestination {
  @Serializable
  data object ChooseInsuranceToAddAddonDestination : AddonPurchaseDestination, Destination

  @Serializable
  data class CustomizeAddon(val insuranceId: String) : AddonPurchaseDestination, Destination

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
}

@Serializable
internal data class SummaryParameters(
  val offerDisplayName: String,
  val quote: TravelAddonQuote,
  val activationDate: LocalDate,
)
