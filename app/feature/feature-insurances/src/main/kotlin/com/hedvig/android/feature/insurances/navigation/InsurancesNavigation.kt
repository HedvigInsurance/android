package com.hedvig.android.feature.insurances.navigation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface InsurancesDestination {
  @Serializable
  data object Graph : InsurancesDestination

  @Serializable
  data object Insurances : InsurancesDestination
}

internal sealed interface InsurancesDestinations {
  @Serializable
  data class InsuranceContractDetail(
    /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer.contract] */
    @SerialName("contractId")
    val contractId: String,
  ) : InsurancesDestinations

  @Serializable
  data object TerminatedInsurances : InsurancesDestinations
}

val insurancesBottomNavPermittedDestinations: List<String> = listOf(
  createRoutePattern<InsurancesDestinations.InsuranceContractDetail>(),
  createRoutePattern<InsurancesDestinations.TerminatedInsurances>(),
)
