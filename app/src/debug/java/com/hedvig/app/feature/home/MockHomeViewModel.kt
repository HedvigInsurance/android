package com.hedvig.app.feature.home

import com.hedvig.app.feature.home.ui.HomeViewModel
import com.hedvig.app.testdata.feature.home.HOME_DATA_PENDING

class MockHomeViewModel : HomeViewModel() {
    init {
        _data.postValue(mockData)
    }

    companion object {
        var mockData = HOME_DATA_PENDING
    }
}
