package com.hedvig.app.feature.insurance

import com.hedvig.app.feature.insurance.ui.tab.InsuranceViewModel
import com.hedvig.app.feature.insurance.ui.tab.items
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_APARTMENT

class MockInsuranceViewModel : InsuranceViewModel() {

    init {
        load()
    }

    override fun load() {
        if (shouldError) {
            shouldError = false
            _viewState.value = ViewState.Error
            return
        }
        _viewState.value = ViewState.Success(items(insuranceMockData))
    }

    override fun markCrossSellsAsSeen() {}

    companion object {
        var insuranceMockData = INSURANCE_DATA_SWEDISH_APARTMENT
        var shouldError = false
    }
}
