package com.hedvig.android.feature.forever.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.android.navigation.core.TopLevelGraphRoot
import kotlinx.serialization.Serializable

@Serializable
data object ForeverKey : HedvigNavKey, TopLevelGraphRoot {
  override val topLevelGraph: TopLevelGraph = TopLevelGraph.Forever
}
