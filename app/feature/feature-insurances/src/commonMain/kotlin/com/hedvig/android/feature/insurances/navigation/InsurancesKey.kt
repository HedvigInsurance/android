package com.hedvig.android.feature.insurances.navigation

import com.hedvig.android.navigation.common.CrossSellEligibleDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.common.TopLevelTabRoot
import kotlinx.serialization.Serializable

@Serializable
data object InsurancesKey : HedvigNavKey, CrossSellEligibleDestination, TopLevelTabRoot {
  override val topLevelTab: TopLevelTab = TopLevelTab.Insurances
}
