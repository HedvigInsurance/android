package com.hedvig.app.feature.embark

import com.hedvig.app.testdata.feature.embark.MORE_OPTIONS_DATA

class MockMoreOptionsViewModel : MoreOptionsViewModel() {
    init {
        load()
    }

    override fun load() {
        if (!shouldLoad) {
            shouldLoad = true
            _data.postValue(Result.failure(Error()))
            return
        }
        _data.postValue(Result.success(mockData))
    }

    companion object {
        var shouldLoad = true
        val mockData = MORE_OPTIONS_DATA
    }
}
