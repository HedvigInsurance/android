package com.hedvig.android.feature.insurances.navigation

import com.hedvig.android.navigation.common.Destination
import kotlin.reflect.KClass
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface InsurancesDestination {
  @Serializable
  data object Graph : InsurancesDestination, Destination

  @Serializable
  data object Insurances : InsurancesDestination, Destination
}

internal sealed interface InsurancesDestinations {
  @Serializable
  data class InsuranceContractDetail(
    /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer.contract] */
    @SerialName("contractId")
    val contractId: String,
  ) : InsurancesDestinations, Destination

  @Serializable
  data object TerminatedInsurances : InsurancesDestinations, Destination
}

val insurancesBottomNavPermittedDestinations: List<KClass<out Destination>> = listOf(
  InsurancesDestinations.InsuranceContractDetail::class,
  InsurancesDestinations.TerminatedInsurances::class,
)

val insurancesCrossSellBottomSheetPermittingDestinations: List<KClass<out Destination>> = listOf(
  InsurancesDestination.Insurances::class,
  InsurancesDestinations.InsuranceContractDetail::class,
)
