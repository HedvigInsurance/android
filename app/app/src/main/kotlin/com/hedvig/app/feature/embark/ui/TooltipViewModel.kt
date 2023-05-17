package com.hedvig.app.feature.embark.ui

import androidx.lifecycle.ViewModel
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics

class TooltipViewModel(
  hAnalytics: HAnalytics,
) : ViewModel() {
  init {
    hAnalytics.screenView(AppScreen.EMBARK_TOOLTIP)
  }
}
