package com.hedvig.app.feature.claims.ui.commonclaim

import androidx.lifecycle.ViewModel
import com.hedvig.hanalytics.HAnalytics

class CommonClaimViewModel(
    id: String,
    hAnalytics: HAnalytics,
) : ViewModel() {
    init {
        hAnalytics.screenViewCommonClaimDetail(id)
    }
}
