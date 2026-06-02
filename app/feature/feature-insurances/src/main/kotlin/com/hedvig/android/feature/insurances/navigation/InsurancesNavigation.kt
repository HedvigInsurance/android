package com.hedvig.android.feature.insurances.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlin.reflect.KClass
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data object InsurancesKey : HedvigNavKey

@Serializable
internal data class InsuranceContractDetailKey(
  /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer.contract] */
  @SerialName("contractId")
  val contractId: String,
) : HedvigNavKey

@Serializable
internal data object TerminatedInsurancesKey : HedvigNavKey

val insurancesBottomNavPermittedDestinations: List<KClass<out HedvigNavKey>> = listOf(
  InsuranceContractDetailKey::class,
  TerminatedInsurancesKey::class,
)

val insurancesCrossSellBottomSheetPermittingDestinations: List<KClass<out HedvigNavKey>> = listOf(
  InsurancesKey::class,
  InsuranceContractDetailKey::class,
)
