package com.hedvig.app.feature.claims.ui.pledge

import androidx.lifecycle.ViewModel
import com.hedvig.hanalytics.HAnalytics

class HonestyPledgeViewModel(
    hAnalytics: HAnalytics,
) : ViewModel() {
    init {
        hAnalytics.screenViewClaimHonorPledge()
    }
}
