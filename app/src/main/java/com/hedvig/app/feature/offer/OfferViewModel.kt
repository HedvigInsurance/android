package com.hedvig.app.feature.offer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import timber.log.Timber

abstract class OfferViewModel: ViewModel() {
    abstract val data: MutableLiveData<OfferQuery.Data>
    abstract val autoStartToken: MutableLiveData<SignOfferMutation.Data>
    abstract val signStatus: MutableLiveData<SignStatusFragment>
    abstract val signError: MutableLiveData<Boolean>
    abstract fun removeDiscount()
    abstract fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data)
    abstract fun triggerOpenChat(done: () -> Unit)
    abstract fun startSign()
    abstract fun clearPreviousErrors()
    abstract fun manuallyRecheckSignStatus()
}

class OfferViewModelImpl(
    private val offerRepository: OfferRepository
) : OfferViewModel() {
    override val data = MutableLiveData<OfferQuery.Data>()
    override val autoStartToken = MutableLiveData<SignOfferMutation.Data>()
    override val signStatus = MutableLiveData<SignStatusFragment>()
    override val signError = MutableLiveData<Boolean>()

    private val disposables = CompositeDisposable()
    private val signStatusSubscriptionHandle = CompositeDisposable()

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
        signStatusSubscriptionHandle.clear()
    }

    override fun removeDiscount() {
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

    override fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data)
        = offerRepository.writeDiscountToCache(data)

    override fun triggerOpenChat(done: () -> Unit) {
        disposables += offerRepository
            .triggerOpenChatFromOffer()
            .subscribe({ done() }, { Timber.e(it) })
    }

    override fun startSign() {
        if (signStatusSubscriptionHandle.size() == 0) {
            signStatusSubscriptionHandle += offerRepository
                .subscribeSignStatus()
                .subscribe({ response ->
                    if (response.hasErrors()) {
                        Timber.e(response.errors().toString())
                        return@subscribe
                    }
                    Timber.i("Data on signStatus subscription: %s", response.data().toString())
                    signStatus.postValue(response.data()?.signStatus?.status?.fragments?.signStatusFragment)
                }, { Timber.e(it) })
        }

        disposables += offerRepository
            .startSign()
            .subscribe({ response ->
                if (response.hasErrors()) {
                    Timber.e(response.errors().toString())
                    signError.postValue(true)
                    return@subscribe
                }

                autoStartToken.postValue(response.data())
            }, { Timber.e(it) })
    }

    override fun clearPreviousErrors() {
        signError.value = false
    }

    override fun manuallyRecheckSignStatus() {
        disposables += offerRepository
            .fetchSignStatus()
            .subscribe({ response ->
                if (response.hasErrors()) {
                    Timber.e(response.errors().toString())
                    signError.postValue(true)
                    return@subscribe
                }
                signStatus.postValue(response.data()?.signStatus?.fragments?.signStatusFragment)
            }, { Timber.e(it) })
    }
}
