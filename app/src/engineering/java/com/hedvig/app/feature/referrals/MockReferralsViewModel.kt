package com.hedvig.app.feature.referrals

import android.os.Handler
import android.os.Looper.getMainLooper
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
            Handler(getMainLooper()).postDelayed(
                {
                    if (!hasLoadedOnce) {
                        hasLoadedOnce = true
                        _data.value = ViewState.Success(
                            topBarState = ViewState.Success.TopBarState(
                                "Test description - Get 500kr when you invite to Hedvig!",
                                "This is a longer content string, " +
                                    "This is a longer content string, " +
                                    "This is a longer content string" +
                                    "This is a longer content string, " +
                                    "This is a longer content string, " +
                                    "This is a longer content string"
                            ),
                            data = referralsData
                        )
                    } else {
                        _data.value = ViewState.Error
                    }
                },
                1000
            )
        } else {
            shouldSucceed = true
            _data.value = ViewState.Error
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
