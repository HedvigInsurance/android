package com.hedvig.app.feature.profile.ui.charity

import androidx.lifecycle.ViewModel
import com.hedvig.hanalytics.HAnalytics

class CharityViewModel(
    hAnalytics: HAnalytics,
) : ViewModel() {
    init {
        hAnalytics.screenViewCharity()
    }
}
