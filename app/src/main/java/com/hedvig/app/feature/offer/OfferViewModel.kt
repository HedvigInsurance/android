package com.hedvig.app.feature.offer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import e
import i
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import org.threeten.bp.LocalDate

abstract class OfferViewModel : ViewModel() {
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
    abstract fun chooseStartDate(id: String, date: LocalDate)
    abstract fun removeStartDate(id: String)
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
                    e { response.errors().toString() }
                    return@subscribe
                }

                data.postValue(response.data())
            }, { e(it) })
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
                    e { response.errors().toString() }
                    return@subscribe
                }

                removeDiscountFromCache()
            }, { e(it) })
    }

    private fun removeDiscountFromCache() {
        offerRepository.removeDiscountFromCache()
    }

    override fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data) =
        offerRepository.writeDiscountToCache(data)

    override fun triggerOpenChat(done: () -> Unit) {
        disposables += offerRepository
            .triggerOpenChatFromOffer()
            .subscribe({ done() }, { e(it) })
    }

    override fun startSign() {
        if (signStatusSubscriptionHandle.size() == 0) {
            signStatusSubscriptionHandle += offerRepository
                .subscribeSignStatus()
                .subscribe({ response ->
                    if (response.hasErrors()) {
                        e { response.errors().toString() }
                        return@subscribe
                    }
                    i { "Data on signStatus subscription:  ${response.data()}" }
                    signStatus.postValue(response.data()?.signStatus?.status?.fragments?.signStatusFragment)
                }, { e(it) })
        }

        disposables += offerRepository
            .startSign()
            .subscribe({ response ->
                if (response.hasErrors()) {
                    e { response.errors().toString() }
                    signError.postValue(true)
                    return@subscribe
                }

                autoStartToken.postValue(response.data())
            }, { e(it) })
    }

    override fun clearPreviousErrors() {
        signError.value = false
    }

    override fun manuallyRecheckSignStatus() {
        disposables += offerRepository
            .fetchSignStatus()
            .subscribe({ response ->
                if (response.hasErrors()) {
                    e { response.errors().toString() }
                    signError.postValue(true)
                    return@subscribe
                }
                signStatus.postValue(response.data()?.signStatus?.fragments?.signStatusFragment)
            }, { e(it) })
    }

    override fun chooseStartDate(id: String, date: LocalDate) {
        disposables += offerRepository
            .chooseStartDate(id, date)
            .subscribe({ response ->
                if (response.hasErrors()) {
                    e { "${response.errors()}" }
                }
                response.data()?.let { data ->
                    offerRepository.writeStartDateToCache(data)
                } ?: run {
                    e { "Missing data when choosing start date" }
                }
            }, {

            })
    }

    override fun removeStartDate(id: String) {
        disposables += offerRepository
            .removeStartDate(id)
            .subscribe({ response ->
                if (response.hasErrors()) {
                    e { response.errors().toString() }
                    return@subscribe
                }
                response.data()?.let { data ->
                    offerRepository.removeStartDateFromCache(data)
                }
            }, { e(it) })
    }
}
