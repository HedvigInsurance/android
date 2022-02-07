package com.hedvig.app.feature.crossselling.ui.detail

import androidx.lifecycle.ViewModel
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.crossselling.ui.CrossSellTracker
import com.hedvig.hanalytics.HAnalytics

class CrossSellDetailViewModel(
    notificationMetadata: CrossSellNotificationMetadata?,
    crossSell: CrossSellData,
    crossSellTracker: CrossSellTracker,
    hAnalytics: HAnalytics,
) : ViewModel() {
    init {
        hAnalytics.screenViewCrossSellDetail(crossSell.typeOfContract)
        if (notificationMetadata != null) {
            crossSellTracker.notificationOpened(
                notificationMetadata,
                crossSell,
            )
        }
    }
}
