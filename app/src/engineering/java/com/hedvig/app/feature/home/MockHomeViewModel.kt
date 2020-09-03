package com.hedvig.app.feature.home

import com.hedvig.app.feature.home.ui.HomeViewModel
import com.hedvig.app.testdata.feature.home.HOME_DATA_PENDING
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_ACTIVE

class MockHomeViewModel : HomeViewModel() {
    init {
        load()
    }

    override fun load() {
        if (shouldError) {
            shouldError = false
            _homeData.postValue(Result.failure(Error()))
            return
        }
        _homeData.postValue(Result.success(homeMockData))
        _payinStatusData.postValue(payinStatusData)
    }

    companion object {
        var shouldError = false
        var homeMockData = HOME_DATA_PENDING
        var payinStatusData = PAYIN_STATUS_DATA_ACTIVE
    }
}
