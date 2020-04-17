package com.hedvig.app.feature.offer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import e
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            offerRepository
                .loadOffer()
                .onEach { response ->
                    data.postValue(response.data())
                }
                .catch { e -> e(e) }
                .collect()
        }
    }

    override fun removeDiscount() {
        viewModelScope.launch {
            val result = runCatching { offerRepository.removeDiscount() }
            if (result.isFailure) {
                result.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            result.getOrNull()?.let { removeDiscountFromCache() }
        }
    }

    private fun removeDiscountFromCache() {
        offerRepository.removeDiscountFromCache()
    }

    override fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data) =
        offerRepository.writeDiscountToCache(data)

    override fun triggerOpenChat(done: () -> Unit) {
        viewModelScope.launch {
            val result = runCatching { offerRepository.triggerOpenChatFromOffer() }
            if (result.isFailure) {
                result.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            result.getOrNull()?.let { done() }
        }
    }

    override fun startSign() {
        viewModelScope.launch {
            offerRepository.subscribeSignStatus()
                .onEach { signStatus.postValue(it.data()?.signStatus?.status?.fragments?.signStatusFragment) }
                .catch { e(it) }
                .launchIn(this)

            val response = runCatching { offerRepository.startSign() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            autoStartToken.postValue(response.getOrNull()?.data())
        }
    }

    override fun clearPreviousErrors() {
        signError.value = false
    }

    override fun manuallyRecheckSignStatus() {
        viewModelScope.launch {
            val response = runCatching { offerRepository.fetchSignStatus() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()
                ?.let { signStatus.postValue(it.data()?.signStatus?.fragments?.signStatusFragment) }
        }
    }

    override fun chooseStartDate(id: String, date: LocalDate) {
        viewModelScope.launch {
            val response = runCatching {
                offerRepository.chooseStartDate(id, date)
            }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data()?.let {
                offerRepository.writeStartDateToCache(it)
            } ?: run {
                e { "Missing data when choosing start date" }
            }
        }
    }

    override fun removeStartDate(id: String) {
        viewModelScope.launch {
            val response = runCatching {
                offerRepository.removeStartDate(id)
            }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data()?.let { offerRepository.removeStartDateFromCache(it) }
        }
    }
}
