package com.hedvig.android.feature.home.home.navigation

import com.hedvig.android.navigation.common.CrossSellEligibleDestination
import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.SuppressesChatPushNotification
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.common.TopLevelTabRoot
import kotlinx.serialization.Serializable

@Serializable
data object HomeKey : HedvigNavKey, CrossSellEligibleDestination, SuppressesChatPushNotification, TopLevelTabRoot {
  override val topLevelTab: TopLevelTab = TopLevelTab.Home
}
