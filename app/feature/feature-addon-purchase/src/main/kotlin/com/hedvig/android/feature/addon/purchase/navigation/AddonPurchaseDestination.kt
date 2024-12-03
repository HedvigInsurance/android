package com.hedvig.android.feature.addon.purchase.navigation

import com.hedvig.android.feature.addon.purchase.data.TravelAddonOption
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDate


@Serializable
data object AddonPurchaseGraphDestination: Destination

internal sealed interface AddonPurchaseDestination {
  /**
   * The start of the flow, where we initiate the flow and choose insurance to add addon to
   */
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
  data object SubmitFailure: AddonPurchaseDestination, Destination
}

@Serializable
internal data class SummaryParameters(
  val quoteId: String,
  val addonId: String,
  val activationDate: LocalDate,
  val displayName: String,
  val chosenTravelAddonOption: TravelAddonOption,
)
