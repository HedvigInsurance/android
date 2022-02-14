package com.hedvig.app.feature.profile.ui.myinfo

import androidx.lifecycle.ViewModel
import com.hedvig.hanalytics.HAnalytics

class MyInfoViewModel(
    hAnalytics: HAnalytics,
) : ViewModel() {
    init {
        hAnalytics.screenViewContactInfo()
    }
}
