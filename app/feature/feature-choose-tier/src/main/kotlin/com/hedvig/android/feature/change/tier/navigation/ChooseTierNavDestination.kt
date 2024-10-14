package com.hedvig.android.feature.change.tier.navigation

import com.hedvig.android.navigation.compose.Destination
import com.hedvig.android.navigation.compose.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

/**
 * The start of the flow, where we have only insurance ID and start the flow as self-service
 */
@Serializable
data class StartTierFlowDestination(
  val insuranceId: String,
) : Destination

/**
 * The start of the flow, where we have can choose insurance to change its tier
 */
@Serializable
data object StartTierFlowChooseInsuranceDestination : Destination

@Serializable
data class ChooseTierGraphDestination(
  /**
   * The ID to the contract and the change tier intent info with activation date, current tier level and all quote ids
   */
  val parameters: InsuranceCustomizationParameters,
) : Destination {
  companion object : DestinationNavTypeAware {
    override val typeList: List<KType> = listOf(typeOf<InsuranceCustomizationParameters>())
  }
}

internal sealed interface ChooseTierDestination {
  @Serializable
  data object SelectTierAndDeductible : ChooseTierDestination, Destination

  @Serializable
  data class Comparison(val quoteIds: List<String>) : ChooseTierDestination, Destination

  @Serializable
  data class Summary(
    val params: SummaryParameters,
  ) : ChooseTierDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SummaryParameters>())
    }
  }

  @Serializable
  data class SubmitSuccess(val activationDate: Int) : ChooseTierDestination, Destination

  @Serializable
  data object SubmitFailure : ChooseTierDestination, Destination
}

@Serializable
data class SummaryParameters(
  val quoteIdToSubmit: String,
  val insuranceId: String,
  val activationDateEpochDays: Int,
)

@Serializable
data class InsuranceCustomizationParameters(
  val insuranceId: String,
  val activationDateEpochDays: Int,
  val currentTierLevel: Int?,
  val currentTierName: String?,
  val quoteIds: List<String>,
)
