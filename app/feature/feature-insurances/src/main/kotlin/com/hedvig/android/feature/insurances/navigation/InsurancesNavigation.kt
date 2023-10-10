package com.hedvig.android.feature.insurances.navigation

import com.kiwi.navigationcompose.typed.Destination
import com.kiwi.navigationcompose.typed.createRoutePattern
import kotlinx.serialization.Serializable

internal sealed interface InsurancesDestination : Destination {
  @Serializable
  data class InsuranceContractDetail(
    val contractId: String,
  ) : InsurancesDestination

  @Serializable
  data object TerminatedInsurances : InsurancesDestination
}

val insurancesBottomNavPermittedDestinations: List<String> = listOf(
  createRoutePattern<InsurancesDestination.InsuranceContractDetail>(),
  createRoutePattern<InsurancesDestination.TerminatedInsurances>(),
)
