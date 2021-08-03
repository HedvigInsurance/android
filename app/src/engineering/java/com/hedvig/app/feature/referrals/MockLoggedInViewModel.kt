package com.hedvig.app.feature.referrals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.testdata.feature.referrals.builders.LoggedInDataBuilder

class MockLoggedInViewModel : LoggedInViewModel() {
    init {
        _data.postValue(
            LoggedInDataBuilder()
                .build()
        )
    }

    override val shouldOpenReviewDialog: LiveData<Boolean> = MutableLiveData(false)
}
