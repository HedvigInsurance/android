package com.hedvig.android.feature.insurances.navigation

import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.createRoutePattern
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data object InsurancesDestination : Destination

internal sealed interface InsurancesDestinations : Destination {
  @Serializable
  data object Insurances : Destination

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
