package com.hedvig.app.feature.insurance

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.insurance.ui.InsuranceViewModel

class MockInsuranceViewModel : InsuranceViewModel() {
    override val data = MutableLiveData<InsuranceQuery.Data>()

    init {
        load()
    }

    fun load() {
        if (hasRenewal) {
    
        }
    }

    companion object {
        val hasRenewal = true
    }
}
