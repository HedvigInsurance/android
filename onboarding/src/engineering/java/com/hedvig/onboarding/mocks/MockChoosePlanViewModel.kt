package com.hedvig.onboarding.mocks

import com.hedvig.app.testdata.feature.onboarding.CHOOSE_PLAN_DATA
import com.hedvig.onboarding.chooseplan.ChoosePlanViewModel

class MockChoosePlanViewModel : ChoosePlanViewModel() {
    init {
        load()
    }

    override fun load() {
        _data.postValue(Result.success(mockData))
    }

    companion object {
        var mockData = CHOOSE_PLAN_DATA.embarkStories
    }
}
