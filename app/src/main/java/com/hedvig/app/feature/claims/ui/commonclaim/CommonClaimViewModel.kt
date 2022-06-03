package com.hedvig.app.feature.claims.ui.commonclaim

import androidx.lifecycle.ViewModel
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics

class CommonClaimViewModel(
    hAnalytics: HAnalytics,
) : ViewModel() {
    init {
        hAnalytics.screenView(AppScreen.COMMON_CLAIM_DETAIL)
    }
}
