package com.hedvig.android.feature.forever.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import com.hedvig.android.navigation.common.TopLevelTab
import com.hedvig.android.navigation.common.TopLevelTabRoot
import kotlinx.serialization.Serializable

@Serializable
data object ForeverKey : HedvigNavKey, TopLevelTabRoot {
  override val topLevelTab: TopLevelTab = TopLevelTab.Forever
}

/**
 * The Forever screen pushed onto whichever run the member is already in, as opposed to [ForeverKey],
 * which *is* the Forever tab's root. Reaching the referral screen from inside another tab has to use
 * this key: pushing a [TopLevelTabRoot] would put a second tab root inside a foreign run, moving the
 * nav bar's selection and breaking the one-tab-per-key assumption the runs model relies on.
 */
@Serializable
data object InviteFriendsKey : HedvigNavKey
