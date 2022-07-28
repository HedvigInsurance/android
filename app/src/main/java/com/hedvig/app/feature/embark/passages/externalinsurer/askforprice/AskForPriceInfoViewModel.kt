package com.hedvig.app.feature.embark.passages.externalinsurer.askforprice

import androidx.lifecycle.ViewModel
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics

class AskForPriceInfoViewModel(
  private val collectionId: String,
  private val hAnalytics: HAnalytics,
) : ViewModel() {

  init {
    hAnalytics.screenView(AppScreen.DATA_COLLECTION_INTRO)
  }

  fun onSkipRetrievePriceInfo() {
    hAnalytics.dataCollectionSkipped(collectionId)
  }
}
