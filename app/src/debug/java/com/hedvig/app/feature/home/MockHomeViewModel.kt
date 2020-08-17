package com.hedvig.app.feature.home

import com.hedvig.app.feature.home.ui.HomeViewModel
import com.hedvig.app.testdata.feature.home.HOME_DATA_PENDING

class MockHomeViewModel : HomeViewModel() {
    init {
        load()
    }

    companion object {
        var shouldError = false
        var mockData = HOME_DATA_PENDING
    }

    override fun load() {
        if (shouldError) {
            shouldError = false
            _data.postValue(Result.failure(Error()))
            return
        }
        _data.postValue(Result.success(mockData))
    }
}
