package com.hedvig.android.feature.change.tier.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.NavKeyTypeAware
import com.hedvig.android.shared.tier.comparison.navigation.ComparisonParameters
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The start of the flow, where we have only insurance ID and start the flow as self-service
 */
@androidx.annotation.Keep
@Serializable
data class StartTierFlowDestination(
  /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer.changeTierWithContractId] */
  @SerialName("contractId")
  val insuranceId: String,
) : HedvigNavKey

/**
 * The start of the flow, where we have can choose insurance to change its tier
 */
@Serializable
data object StartTierFlowChooseInsuranceDestination : HedvigNavKey

@Serializable
data class ChooseTierGraphDestination(
  /**
   * The ID to the contract and the change tier intent info with activation date, current tier level and all quote ids
   */
  val parameters: InsuranceCustomizationParameters,
) : HedvigNavKey {
  companion object : NavKeyTypeAware {
    override val typeList: List<KType> = listOf(typeOf<InsuranceCustomizationParameters>())
  }
}

internal sealed interface ChooseTierDestination {
  @Serializable
  data class Comparison(val comparisonParameters: ComparisonParameters) : ChooseTierDestination, HedvigNavKey {
    companion object : NavKeyTypeAware {
      override val typeList: List<KType> = listOf(typeOf<ComparisonParameters>())
    }
  }

  @Serializable
  data class Summary(
    val params: SummaryParameters,
  ) : ChooseTierDestination, HedvigNavKey {
    companion object : NavKeyTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SummaryParameters>())
    }
  }

  @Serializable
  data class SubmitSuccess(val activationDate: LocalDate) : ChooseTierDestination, HedvigNavKey {
    companion object : NavKeyTypeAware {
      override val typeList: List<KType> = listOf(
        typeOf<LocalDate>(),
      )
    }
  }

  @Serializable
  data object SubmitFailure : ChooseTierDestination, HedvigNavKey
}

@Serializable
data class SummaryParameters(
  val quoteIdToSubmit: String,
  val insuranceId: String,
  val activationDate: LocalDate,
)

@Serializable
data class InsuranceCustomizationParameters(
  val insuranceId: String,
  val activationDate: LocalDate,
  val quoteIds: List<String>,
)
