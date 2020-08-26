package com.hedvig.app.feature.insurance

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.insurance.ui.InsuranceViewModel
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA

class MockInsuranceViewModel : InsuranceViewModel() {
    override val data = MutableLiveData<Result<InsuranceQuery.Data>>()

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
