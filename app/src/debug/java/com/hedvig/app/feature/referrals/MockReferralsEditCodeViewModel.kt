package com.hedvig.app.feature.referrals

import android.os.Handler
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeViewModel
import com.hedvig.app.testdata.feature.referrals.EDIT_CODE_DATA_SUCCESS

class MockReferralsEditCodeViewModel : ReferralsEditCodeViewModel() {
    override fun changeCode(newCode: String) {
        Handler().postDelayed({
            if (!shouldSucceed) {
                _data.postValue(Result.failure(Error()))
                return@postDelayed
            }

            _data.postValue(Result.success(EDIT_CODE_DATA_SUCCESS))
        }, 1000)
    }

    companion object {
        var shouldSucceed = false
    }
}
