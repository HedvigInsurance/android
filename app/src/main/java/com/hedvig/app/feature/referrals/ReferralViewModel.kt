package com.hedvig.app.feature.referrals

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import e
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign

class ReferralViewModel(
    private val referralRepository: ReferralRepository
) :
    ViewModel() {

    val redeemCodeStatus: MutableLiveData<RedeemReferralCodeMutation.Data> = MutableLiveData()
    private val disposables = CompositeDisposable()

    fun redeemReferralCode(code: String) {
        disposables += referralRepository
            .redeemReferralCode(code)
            .subscribe({
                if (it.hasErrors()) {
                    redeemCodeStatus.postValue(null)
                } else {
                    redeemCodeStatus.postValue(it.data())
                }
            }, { error ->
                redeemCodeStatus.postValue(null)
                e { "$error Failed to redeem code" }
            })
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
