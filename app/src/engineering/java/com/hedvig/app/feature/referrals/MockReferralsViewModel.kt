package com.hedvig.app.feature.referrals

import android.os.Handler
import com.hedvig.app.feature.referrals.ui.tab.ReferralsViewModel
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS

class MockReferralsViewModel : ReferralsViewModel() {
    init {
        if (loadInitially) {
            load()
        }
    }

    override fun load() {
        if (shouldSucceed) {
            Handler().postDelayed({
                if (!hasLoadedOnce) {
                    hasLoadedOnce = true
                    _data.postValue(Result.success(referralsData))
                } else {
                    _data.postValue(Result.success(afterRefreshData))
                }
            }, 1000)
        } else {
            shouldSucceed = true
            _data.postValue(Result.failure(Error("Something went wrong")))
        }
        _isRefreshing.postValue(false)
    }

    companion object {
        var loadInitially = false
        var shouldSucceed = false
        var referralsData = REFERRALS_DATA_WITH_NO_DISCOUNTS
        var hasLoadedOnce = false
        var afterRefreshData = referralsData
    }
}
