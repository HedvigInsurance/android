package com.hedvig.app.feature.insurance

import com.hedvig.app.feature.insurance.ui.InsuranceViewModel
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA

class MockInsuranceViewModel : InsuranceViewModel() {

    init {
        load()
    }

    override fun load() {
        if (shouldError) {
            shouldError = false
            data.postValue(Result.failure(Error()))
            return
        }
        data.postValue(Result.success(insuranceMockData))
    }

    companion object {
        var insuranceMockData = INSURANCE_DATA
        var shouldError = false
    }
}
