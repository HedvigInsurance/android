package com.hedvig.app.feature.referrals

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import e
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch

class ReferralViewModel(
    private val referralRepository: ReferralRepository
) :
    ViewModel() {

    val redeemCodeStatus: MutableLiveData<RedeemReferralCodeMutation.Data> = MutableLiveData()
    private val disposables = CompositeDisposable()

    fun redeemReferralCode(code: String) {
        viewModelScope.launch {
            val response = runCatching { referralRepository.redeemReferralCode(code) }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                redeemCodeStatus.postValue(null)
                return@launch
            }
            response.getOrNull()?.let { redeemCodeStatus.postValue(it.data()) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
