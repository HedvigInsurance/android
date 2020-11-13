package com.hedvig.app.feature.referrals

import android.os.Handler
import android.os.Looper.getMainLooper
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.feature.referrals.ui.activated.ReferralsActivatedViewModel
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED

class MockReferralsActivatedViewModel : ReferralsActivatedViewModel() {
    override val data = MutableLiveData<LoggedInQuery.Data>()

    init {
        Handler(getMainLooper()).postDelayed({
            data.postValue(LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED)
        }, loadDelay)
    }

    companion object {
        var loadDelay = 1000L
    }
}
