package com.hedvig.app.feature.profile.ui.aboutapp

import androidx.lifecycle.ViewModel
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics

class AboutAppViewModel(
    hAnalytics: HAnalytics,
) : ViewModel() {
    init {
        hAnalytics.screenView(AppScreen.APP_INFORMATION)
    }
}
