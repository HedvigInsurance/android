package com.hedvig.app.feature.referrals

import android.os.Handler
import android.os.Looper.getMainLooper
import com.hedvig.app.feature.referrals.ui.editcode.ReferralsEditCodeViewModel
import com.hedvig.app.testdata.feature.referrals.EDIT_CODE_DATA_ALREADY_TAKEN
import com.hedvig.app.testdata.feature.referrals.EDIT_CODE_DATA_SUCCESS
import com.hedvig.app.testdata.feature.referrals.EDIT_CODE_DATA_TOO_LONG
import com.hedvig.app.testdata.feature.referrals.EDIT_CODE_DATA_TOO_MANY_CHANGES
import com.hedvig.app.testdata.feature.referrals.EDIT_CODE_DATA_TOO_SHORT
import com.hedvig.app.testdata.feature.referrals.EDIT_CODE_DATA_UNKNOWN_RESULT
import com.hedvig.app.testdata.feature.referrals.builders.EditCodeDataBuilder

class MockReferralsEditCodeViewModel : ReferralsEditCodeViewModel() {
  override fun changeCode(newCode: String) {
    _isSubmitting.postValue(true)
    if (!shouldLoad) {
      return
    }
    Handler(getMainLooper()).postDelayed(
      {
        _isSubmitting.postValue(false)
        if (!shouldSucceed) {
          _data.value = ViewState.Error
          return@postDelayed
        }

        _data.value = ViewState.Success(
          when (variant) {
            EditCodeDataBuilder.ResultVariant.SUCCESS -> EDIT_CODE_DATA_SUCCESS
            EditCodeDataBuilder.ResultVariant.ALREADY_TAKEN -> EDIT_CODE_DATA_ALREADY_TAKEN
            EditCodeDataBuilder.ResultVariant.TOO_SHORT -> EDIT_CODE_DATA_TOO_SHORT
            EditCodeDataBuilder.ResultVariant.TOO_LONG -> EDIT_CODE_DATA_TOO_LONG
            EditCodeDataBuilder.ResultVariant.EXCEEDED_MAX_UPDATES -> EDIT_CODE_DATA_TOO_MANY_CHANGES
            EditCodeDataBuilder.ResultVariant.UNKNOWN -> EDIT_CODE_DATA_UNKNOWN_RESULT
          },
        )
      },
      1000,
    )
  }

  companion object {
    var shouldLoad = false
    var shouldSucceed = false
    var variant = EditCodeDataBuilder.ResultVariant.SUCCESS
  }
}
