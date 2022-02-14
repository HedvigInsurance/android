package com.hedvig.app.feature.home

import com.hedvig.app.feature.home.ui.HomeViewModel
import com.hedvig.app.feature.tracking.MockHAnalytics
import com.hedvig.app.testdata.feature.home.HOME_DATA_PENDING
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_ACTIVE

class MockHomeViewModel : HomeViewModel(MockHAnalytics()) {
    init {
        load()
    }

    override fun load() {
        if (shouldError) {
            shouldError = false
            _homeData.postValue(ViewState.Error)
            return
        }
        _homeData.postValue(ViewState.Success(homeMockData))
        _payinStatusData.postValue(payinStatusData)
    }

    override fun reload() {
        load()
    }

    companion object {
        var shouldError = false
        var homeMockData = HOME_DATA_PENDING
        var payinStatusData = PAYIN_STATUS_DATA_ACTIVE
    }
}
