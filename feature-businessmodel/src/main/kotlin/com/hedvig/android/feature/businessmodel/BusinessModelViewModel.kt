package com.hedvig.android.feature.businessmodel

import androidx.lifecycle.ViewModel
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics

internal class BusinessModelViewModel(
  hAnalytics: HAnalytics,
) : ViewModel() {

  init {
    hAnalytics.screenView(AppScreen.CHARITY)
  }
}
