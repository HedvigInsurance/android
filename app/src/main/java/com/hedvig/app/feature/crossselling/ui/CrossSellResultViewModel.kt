package com.hedvig.app.feature.crossselling.ui

import androidx.lifecycle.ViewModel

class CrossSellResultViewModel(
    result: CrossSellingResult,
    tracker: CrossSellTracker,
) : ViewModel() {
    init {
        tracker.view(result)
    }
}
