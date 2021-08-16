package com.hedvig.app.feature.referrals

import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.testdata.feature.referrals.builders.LoggedInDataBuilder

class MockLoggedInViewModel : LoggedInViewModel() {
    init {
        _data.postValue(
            LoggedInDataBuilder()
                .build()
        )
    }

    override fun onReviewByChatComplete() {}
}
