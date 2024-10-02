package com.hedvig.android.feature.change.tier.navigation

import com.hedvig.android.navigation.compose.Destination
import com.hedvig.android.navigation.compose.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChooseTierGraphDestination(
  /**
   * The ID to the contract and the change tier intent info with activation date, current tier level and all quotes
   */
  @SerialName("customization_params")
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
  data class ChangingTierSuccess(val activationDate: LocalDate) : ChooseTierDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<LocalDate>())
    }
  }

  @Serializable
  data class Comparison(val quoteIds: List<String>) : ChooseTierDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<String>>()) // todo: quote or list of quotes here?
    }
  }

  @Serializable
  data class Summary(
    val quoteIdToSubmit: String,
    // todo: also activation date, maybe???
  ) : ChooseTierDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<String>())
    }
  }
}

@Serializable
data class InsuranceCustomizationParameters(
  val insuranceId: String,
  val activationDate: LocalDate,
  val currentTierLevel: Int?,
  val currentTierName: String?,
  val quoteIds: List<String>,
)
