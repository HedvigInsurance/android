package com.hedvig.app.feature.onboarding

import com.hedvig.app.testdata.feature.onboarding.CHOOSE_PLAN_DATA

class MockChoosePlanViewModel : ChoosePlanViewModel() {
    init {
        load()
    }

    override fun load() {
        _viewState.value = ViewState.Success(mockData)
    }

    companion object {
        var mockData = CHOOSE_PLAN_DATA.mapToSuccess().toModel()
    }
}
