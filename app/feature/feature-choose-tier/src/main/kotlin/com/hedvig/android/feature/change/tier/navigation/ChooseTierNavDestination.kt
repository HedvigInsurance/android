package com.hedvig.android.feature.change.tier.navigation

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.navigation.NavType
import com.hedvig.android.navigation.compose.Destination
import com.hedvig.android.navigation.compose.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * The start of the flow, where we have only insurance ID and start the flow as self-service
 */
@Serializable
data class StartTierFlowDestination(
  val insuranceId: String,
) : Destination {
  companion object : DestinationNavTypeAware {
    override val typeList: List<KType> = listOf(typeOf<String>())
  }
}

@Serializable
data class ChooseTierGraphDestination(
  /**
   * The ID to the contract and the change tier intent info with activation date, current tier level and all quote ids
   */
  @SerialName("customization_params")
  val parameters: InsuranceCustomizationParameters,
) : Destination {
  companion object : DestinationNavTypeAware {
    override val typeList: List<KType> = listOf(typeOf<InsuranceCustomizationParametersType>())
  }
}

internal sealed interface ChooseTierDestination {
  @Serializable
  data object SelectTierAndDeductible : ChooseTierDestination, Destination

  @Serializable
  data class Comparison(val quoteIds: List<String>) : ChooseTierDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<List<String>>())
    }
  }

  @Serializable
  data class Summary(
    @SerialName("tier_summary_params")
    val params: SummaryParameters) : ChooseTierDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SummaryParametersType>())
    }
  }

  @Serializable
  data class SubmitSuccess(val activationDate: Int) : ChooseTierDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<Int>())
    }
  }

  @Serializable
  data object SubmitFailure: ChooseTierDestination, Destination
}

@Serializable
@Parcelize
data class SummaryParameters(
  val quoteIdToSubmit: String,
  val insuranceId: String,
  val activationDateEpochDays: Int,
) : Parcelable

@Serializable
@Parcelize
data class InsuranceCustomizationParameters(
  val insuranceId: String,
  val activationDateEpochDays: Int,
  val currentTierLevel: Int?,
  val currentTierName: String?,
  val quoteIds: List<String>,
) : Parcelable

class InsuranceCustomizationParametersType() : NavType<InsuranceCustomizationParameters>(
  isNullableAllowed = false,
) {
  override fun get(bundle: Bundle, key: String): InsuranceCustomizationParameters? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      bundle.getParcelable(key, InsuranceCustomizationParameters::class.java)
    } else {
      @Suppress("DEPRECATION")
      bundle.getParcelable(key)
    }
  }

  override fun parseValue(value: String): InsuranceCustomizationParameters {
    return Json.decodeFromString<InsuranceCustomizationParameters>(value)
  }

  override fun serializeAsValue(value: InsuranceCustomizationParameters): String {
    return Json.encodeToString(value)
  }

  override fun put(bundle: Bundle, key: String, value: InsuranceCustomizationParameters) {
    bundle.putParcelable(key, value)
  }
}

 class SummaryParametersType() : NavType<SummaryParameters>(
  isNullableAllowed = false,
) {
  override fun get(bundle: Bundle, key: String): SummaryParameters? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      bundle.getParcelable(key, SummaryParameters::class.java)
    } else {
      @Suppress("DEPRECATION")
      bundle.getParcelable(key)
    }
  }

  override fun parseValue(value: String): SummaryParameters {
    return Json.decodeFromString<SummaryParameters>(value)
  }

  override fun serializeAsValue(value: SummaryParameters): String {
    return Json.encodeToString(value)
  }

  override fun put(bundle: Bundle, key: String, value: SummaryParameters) {
    bundle.putParcelable(key, value)
  }
}

