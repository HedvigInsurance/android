package com.hedvig.app.feature.referrals

import android.os.Handler
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeViewModel
import com.hedvig.app.testdata.feature.referrals.EDIT_CODE_DATA_ALREADY_TAKEN
import com.hedvig.app.testdata.feature.referrals.EDIT_CODE_DATA_SUCCESS
import com.hedvig.app.testdata.feature.referrals.EDIT_CODE_DATA_TOO_LONG
import com.hedvig.app.testdata.feature.referrals.EDIT_CODE_DATA_TOO_MANY_CHANGES
import com.hedvig.app.testdata.feature.referrals.EDIT_CODE_DATA_TOO_SHORT
import com.hedvig.app.testdata.feature.referrals.builders.EditCodeDataBuilder

class MockReferralsEditCodeViewModel : ReferralsEditCodeViewModel() {
    override fun changeCode(newCode: String) {
        Handler().postDelayed({
            if (!shouldSucceed) {
                _data.postValue(Result.failure(Error()))
                return@postDelayed
            }

            _data.postValue(
                Result.success(
                    when (variant) {
                        EditCodeDataBuilder.ResultVariant.SUCCESS -> EDIT_CODE_DATA_SUCCESS
                        EditCodeDataBuilder.ResultVariant.ALREADY_TAKEN -> EDIT_CODE_DATA_ALREADY_TAKEN
                        EditCodeDataBuilder.ResultVariant.TOO_SHORT -> EDIT_CODE_DATA_TOO_SHORT
                        EditCodeDataBuilder.ResultVariant.TOO_LONG -> EDIT_CODE_DATA_TOO_LONG
                        EditCodeDataBuilder.ResultVariant.EXCEEDED_MAX_UPDATES -> EDIT_CODE_DATA_TOO_MANY_CHANGES
                    }
                )
            )
        }, 1000)
    }

    companion object {
        var shouldSucceed = false
        var variant = EditCodeDataBuilder.ResultVariant.SUCCESS
    }
}
