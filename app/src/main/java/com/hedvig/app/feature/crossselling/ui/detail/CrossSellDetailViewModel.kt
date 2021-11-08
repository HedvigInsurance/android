package com.hedvig.app.feature.crossselling.ui.detail

import androidx.lifecycle.ViewModel
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.crossselling.ui.CrossSellTracker

class CrossSellDetailViewModel(
    openedFromNotification: Boolean,
    crossSell: CrossSellData,
    crossSellTracker: CrossSellTracker,
) : ViewModel() {
    init {
        if (openedFromNotification) {
            crossSellTracker.notificationOpened(crossSell)
        }
    }
}
