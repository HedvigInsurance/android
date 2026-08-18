package com.hedvig.android.feature.forever.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.common.TopLevelTabRoot
import kotlinx.serialization.Serializable

@Serializable
data object ForeverKey : HedvigNavKey, TopLevelTabRoot {
  override val topLevelTab: TopLevelTab = TopLevelTab.Forever
}
