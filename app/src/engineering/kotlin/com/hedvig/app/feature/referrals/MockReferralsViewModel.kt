package com.hedvig.app.feature.referrals

import android.os.Handler
import android.os.Looper.getMainLooper
import com.hedvig.app.feature.referrals.ui.tab.ReferralsUiState
import com.hedvig.app.feature.referrals.ui.tab.ReferralsViewModel
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockReferralsViewModel : ReferralsViewModel() {

  private val _data = MutableStateFlow<ReferralsUiState>(ReferralsUiState.Loading)
  override val data: StateFlow<ReferralsUiState> = _data.asStateFlow()

  init {
    if (loadInitially) {
      reload()
    }
  }

  override fun reload() {
    _data.value = ReferralsUiState.Loading
    if (shouldSucceed) {
      Handler(getMainLooper()).postDelayed(
        {
          if (!hasLoadedOnce) {
            hasLoadedOnce = true
            _data.value = ReferralsUiState.Success(
              data = referralsData,
              isLoading = false,
            )
          } else {
            _data.value = ReferralsUiState.Error(false)
          }
        },
        1000,
      )
    } else {
      shouldSucceed = true
      _data.value = ReferralsUiState.Error(false)
    }
  }

  companion object {
    var loadInitially = false
    var shouldSucceed = false
    var referralsData = REFERRALS_DATA_WITH_NO_DISCOUNTS
    var hasLoadedOnce = false
  }
}
