package com.hedvig.onboarding.mocks

import com.hedvig.app.testdata.feature.onboarding.MEMBER_ID_DATA
import com.hedvig.onboarding.moreoptions.MoreOptionsViewModel

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
        val mockData = MEMBER_ID_DATA
    }
}
