package com.hedvig.android.feature.insurances.navigation

import com.hedvig.android.navigation.common.CrossSellEligibleDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.core.DeepLinkAncestry
import com.hedvig.android.navigation.core.TopLevelGraph
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data object InsurancesKey : HedvigNavKey, CrossSellEligibleDestination

@Serializable
internal data class InsuranceContractDetailKey(
  /** Must match the name of the param inside [com.hedvig.android.navigation.core.HedvigDeepLinkContainer.contract] */
  @SerialName("contractId")
  val contractId: String,
) : HedvigNavKey, DeepLinkAncestry, CrossSellEligibleDestination {
  override val owningTab = TopLevelGraph.Insurances
  override val syntheticParents = emptyList<HedvigNavKey>()
}

@Serializable
internal data object TerminatedInsurancesKey : HedvigNavKey
