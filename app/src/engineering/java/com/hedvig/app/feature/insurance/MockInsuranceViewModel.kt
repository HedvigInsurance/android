package com.hedvig.app.feature.insurance

import com.hedvig.app.feature.insurance.ui.InsuranceViewModel
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_APARTMENT

class MockInsuranceViewModel : InsuranceViewModel() {

    init {
        load()
    }

    override fun load() {
        if (shouldError) {
            shouldError = false
            _data.value = ViewState.Error
            return
        }
        _data.value = ViewState.Success(insuranceMockData)
    }

    companion object {
        var insuranceMockData = INSURANCE_DATA_SWEDISH_APARTMENT
        var shouldError = false
    }
}
