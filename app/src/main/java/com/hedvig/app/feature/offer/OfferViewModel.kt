package com.hedvig.app.feature.offer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import com.hedvig.android.owldroid.graphql.SignStatusSubscription
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber

class OfferViewModel(
    private val offerRepository: OfferRepository
) : ViewModel() {
    val data = MutableLiveData<OfferQuery.Data>()
    val autoStartToken = MutableLiveData<SignOfferMutation.Data>()
    val signStatus = MutableLiveData<SignStatusSubscription.Data>()

    private val disposables = CompositeDisposable()

    init {
        load()
    }

    fun load() {
        disposables += offerRepository
            .loadOffer()
            .subscribe({ response ->
                if (response.hasErrors()) {
                    Timber.e(response.errors().toString())
                    return@subscribe
                }

                data.postValue(response.data())
            }, { Timber.e(it) })
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    fun removeDiscount() {
        disposables += offerRepository
            .removeDiscount()
            .subscribe({ response ->
                if (response.hasErrors()) {
                    Timber.e(response.errors().toString())
                    return@subscribe
                }

                removeDiscountFromCache()
            }, { Timber.e(it) })
    }

    private fun removeDiscountFromCache() {
        offerRepository.removeDiscountFromCache()
    }

    fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data) = offerRepository.writeDiscountToCache(data)

    fun triggerOpenChat(done: () -> Unit) {
        disposables += offerRepository
            .triggerOpenChatFromOffer()
            .subscribe({ done() }, { Timber.e(it) })
    }
    fun startSign() {
        disposables += offerRepository
            .subscribeSignStatus()
            .subscribe({ response ->
                signStatus.postValue(response.data())
            }, { Timber.e(it) })

        disposables += offerRepository
            .startSign()
            .subscribe({ response ->
                if (response.hasErrors()) {
                    Timber.e(response.errors().toString())
                    return@subscribe
                }

                autoStartToken.postValue(response.data())
            }, { Timber.e(it) })
    }
}
