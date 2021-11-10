package com.hedvig.app.feature.crossselling.ui.detail

import androidx.lifecycle.ViewModel
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.crossselling.ui.CrossSellTracker

class CrossSellDetailViewModel(
    notificationMetadata: CrossSellNotificationMetadata?,
    crossSell: CrossSellData,
    crossSellTracker: CrossSellTracker,
) : ViewModel() {
    init {
        if (notificationMetadata != null) {
            crossSellTracker.notificationOpened(
                notificationMetadata,
                crossSell,
            )
        }
    }
}
