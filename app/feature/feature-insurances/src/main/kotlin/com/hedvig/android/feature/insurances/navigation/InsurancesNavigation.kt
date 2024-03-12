package com.hedvig.android.feature.insurances.navigation

import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.createRoutePattern
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface InsurancesDestination : Destination {
  @Serializable
  data class InsuranceContractDetail(
    /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer.contract] */
    @SerialName("contractId")
    val contractId: String,
  ) : InsurancesDestination

  @Serializable
  data object TerminatedInsurances : InsurancesDestination
}

val insurancesBottomNavPermittedDestinations: List<String> = listOf(
  createRoutePattern<InsurancesDestination.InsuranceContractDetail>(),
  createRoutePattern<InsurancesDestination.TerminatedInsurances>(),
)
