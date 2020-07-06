package com.hedvig.app.feature.referrals

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.testdata.feature.referrals.builders.LoggedInDataBuilder

class MockLoggedInViewModel : LoggedInViewModel() {
    override val data = MutableLiveData<LoggedInQuery.Data>()

    init {
        data.postValue(
            LoggedInDataBuilder()
                .build()
        )
    }
}
