package com.hedvig.android.feature.home.claims.commonclaim

import androidx.lifecycle.ViewModel
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics

internal class CommonClaimViewModel(
  hAnalytics: HAnalytics,
) : ViewModel() {
  init {
    hAnalytics.screenView(AppScreen.COMMON_CLAIM_DETAIL)
  }
}
