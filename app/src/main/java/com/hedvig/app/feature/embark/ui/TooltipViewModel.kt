package com.hedvig.app.feature.embark.ui

import androidx.lifecycle.ViewModel
import com.hedvig.hanalytics.HAnalytics

class TooltipViewModel(
    hAnalytics: HAnalytics,
) : ViewModel() {
    init {
        hAnalytics.screenViewEmbarkTooltip()
    }
}
