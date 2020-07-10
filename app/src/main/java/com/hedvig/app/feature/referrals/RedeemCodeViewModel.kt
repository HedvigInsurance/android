package com.hedvig.app.feature.referrals

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import e
import kotlinx.coroutines.launch

class RedeemCodeViewModel(
    private val referralRepository: ReferralRepository
) : ViewModel() {

    val redeemCodeStatus: MutableLiveData<RedeemReferralCodeMutation.Data> = MutableLiveData()

    fun redeemReferralCode(code: String) {
        viewModelScope.launch {
            val response = runCatching { referralRepository.redeemReferralCodeAsync(code).await() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                redeemCodeStatus.postValue(null)
                return@launch
            }
            response.getOrNull()?.let { redeemCodeStatus.postValue(it.data) }
        }
    }
}
