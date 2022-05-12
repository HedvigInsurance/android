package com.hedvig.app.feature.crossselling.ui.detail

import androidx.lifecycle.ViewModel
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.hanalytics.HAnalytics

class CrossSellDetailViewModel(
    notificationMetadata: CrossSellNotificationMetadata?,
    crossSell: CrossSellData,
    hAnalytics: HAnalytics,
) : ViewModel() {
    init {
        hAnalytics.screenViewCrossSellDetail(crossSell.typeOfContract)
    }
}
