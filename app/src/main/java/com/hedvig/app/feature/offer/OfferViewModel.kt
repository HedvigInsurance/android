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
import java.time.LocalDate

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
                .offer()
                .onEach { response ->
                    response.data?.let { data.postValue(it) }
                }
                .catch { e(it) }
                .collect()
        }
    }

    override fun removeDiscount() {
        viewModelScope.launch {
            val result = runCatching { offerRepository.removeDiscountAsync().await() }
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
            val result = runCatching { offerRepository.triggerOpenChatFromOfferAsync().await() }
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
                .onEach { response ->
                    response.data?.signStatus?.status?.fragments?.signStatusFragment?.let {
                        signStatus.postValue(
                            it
                        )
                    }
                }
                .catch { e(it) }
                .launchIn(this)

            val response = runCatching { offerRepository.startSignAsync().await() }
            if (response.isFailure || response.getOrNull()?.hasErrors() == true) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.let { autoStartToken.postValue(it) }
        }
    }

    override fun clearPreviousErrors() {
        signError.value = false
    }

    override fun manuallyRecheckSignStatus() {
        viewModelScope.launch {
            val response = runCatching { offerRepository.fetchSignStatusAsync().await() }
            if (response.isFailure || response.getOrNull()?.hasErrors() == true) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.signStatus?.fragments?.signStatusFragment
                ?.let { signStatus.postValue(it) }
        }
    }

    override fun chooseStartDate(id: String, date: LocalDate) {
        viewModelScope.launch {
            val response = runCatching {
                offerRepository.chooseStartDateAsync(id, date).await()
            }
            if (response.isFailure || response.getOrNull()?.hasErrors() == true) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.let {
                offerRepository.writeStartDateToCache(it)
            } ?: run {
                e { "Missing data when choosing start date" }
            }
        }
    }

    override fun removeStartDate(id: String) {
        viewModelScope.launch {
            val response = runCatching {
                offerRepository.removeStartDateAsync(id).await()
            }
            if (response.isFailure || response.getOrNull()?.hasErrors() == true) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.let { offerRepository.removeStartDateFromCache(it) }
        }
    }
}
