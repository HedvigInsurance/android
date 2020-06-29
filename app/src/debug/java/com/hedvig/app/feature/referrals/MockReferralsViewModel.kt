package com.hedvig.app.feature.referrals

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS

class MockReferralsViewModel : ReferralsViewModel() {
    override val data = MutableLiveData<Result<ReferralsQuery.Data>>()

    init {
        if (loadInitially) {
            load()
        }
    }

    override fun load() {
        if (shouldSucceed) {
            data.postValue(Result.success(referralsData))
        } else {
            shouldSucceed = true
            data.postValue(Result.failure(Error("Something went wrong")))
        }
    }

    companion object {
        var loadInitially = false
        var shouldSucceed = false
        var referralsData = REFERRALS_DATA_WITH_NO_DISCOUNTS
    }
}
