package com.hedvig.android.feature.home.claims.pledge

import androidx.lifecycle.ViewModel
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics

internal class HonestyPledgeViewModel(
  hAnalytics: HAnalytics,
) : ViewModel() {
  init {
    hAnalytics.screenView(AppScreen.CLAIM_HONOR_PLEDGE)
  }
}
