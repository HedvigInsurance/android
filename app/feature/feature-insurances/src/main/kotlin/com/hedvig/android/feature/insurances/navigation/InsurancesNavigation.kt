package com.hedvig.android.feature.insurances.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlin.reflect.KClass
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface InsurancesDestination {
  @Serializable
  data object Graph : InsurancesDestination, HedvigNavKey

  @Serializable
  data object Insurances : InsurancesDestination, HedvigNavKey
}

internal sealed interface InsurancesDestinations {
  @Serializable
  data class InsuranceContractDetail(
    /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer.contract] */
    @SerialName("contractId")
    val contractId: String,
  ) : InsurancesDestinations, HedvigNavKey

  @Serializable
  data object TerminatedInsurances : InsurancesDestinations, HedvigNavKey
}

val insurancesBottomNavPermittedDestinations: List<KClass<out HedvigNavKey>> = listOf(
  InsurancesDestinations.InsuranceContractDetail::class,
  InsurancesDestinations.TerminatedInsurances::class,
)

val insurancesCrossSellBottomSheetPermittingDestinations: List<KClass<out HedvigNavKey>> = listOf(
  InsurancesDestination.Insurances::class,
  InsurancesDestinations.InsuranceContractDetail::class,
)
