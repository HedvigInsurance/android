package com.hedvig.app.feature.offer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Response
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.offer.ui.OfferModel
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import e
import java.time.LocalDate
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class OfferViewModel : ViewModel() {
    protected val _viewState = MutableLiveData<ViewState>()
    val viewState: LiveData<ViewState> = _viewState
    abstract val autoStartToken: LiveData<SignOfferMutation.Data>
    abstract val signStatus: LiveData<SignStatusFragment>
    abstract val signError: LiveData<Boolean>
    abstract fun removeDiscount()
    abstract fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data)
    abstract fun triggerOpenChat(done: () -> Unit)
    abstract fun startSign()
    abstract fun clearPreviousErrors()
    abstract fun manuallyRecheckSignStatus()
    abstract fun chooseStartDate(id: String, date: LocalDate)
    abstract fun removeStartDate(id: String)

    sealed class ViewState {
        data class OfferItems(
            val offerItems: List<OfferModel>,
            val documents: List<DocumentItems>
        ) : ViewState()

        sealed class Error : ViewState() {
            data class GeneralError(val message: String?) : Error()
            object EmptyResponse : Error()
        }

        object HasContracts : ViewState()
    }
}

class OfferViewModelImpl(
    _quoteIds: List<String>,
    private val offerRepository: OfferRepository,
) : OfferViewModel() {

    private lateinit var quoteIds: List<String>

    override val autoStartToken = MutableLiveData<SignOfferMutation.Data>()
    override val signStatus = MutableLiveData<SignStatusFragment>()
    override val signError = MutableLiveData<Boolean>()

    init {
        if (_quoteIds.isEmpty()) {
            viewModelScope.launch {
                val idResult = offerRepository.quoteIdOfLastQuoteOfMember().safeQuery()
                if (idResult !is QueryResult.Success) {
                    // TODO: Error UI
                    return@launch
                }
                val id = idResult.data.lastQuoteOfMember.asCompleteQuote?.id
                if (id == null) {
                    // TODO: Error UI
                    return@launch
                }
                quoteIds = listOf(id)
                load()
            }
        } else {
            quoteIds = _quoteIds
            load()
        }
    }

    fun load() {
        offerRepository.offer(quoteIds)
            .map(::toViewState)
            .onEach(_viewState::postValue)
            .catch { _viewState.postValue(ViewState.Error.GeneralError(it.message)) }
            .launchIn(viewModelScope)
    }

    private fun toViewState(response: Response<OfferQuery.Data>): ViewState {
        return response.errors?.let {
            ViewState.Error.GeneralError(it.firstOrNull()?.message)
        } ?: response.data?.let { data ->
            if (data.contracts.isNotEmpty()) {
                ViewState.HasContracts
            } else {
                val offerItems = OfferItemsBuilder.createOfferItems(data)
                val documentItems = OfferItemsBuilder.createDocumentItems(data)
                ViewState.OfferItems(offerItems, documentItems)
            }
        } ?: ViewState.Error.EmptyResponse
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
        offerRepository.removeDiscountFromCache(quoteIds)
    }

    override fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data) =
        offerRepository.writeDiscountToCache(quoteIds, data)

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
                .onEach { response ->
                    response.data?.signStatus?.status?.fragments?.signStatusFragment?.let {
                        signStatus.postValue(
                            it
                        )
                    }
                }
                .catch { e(it) }
                .launchIn(this)

            val response = runCatching { offerRepository.startSign() }
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
            val response = runCatching { offerRepository.fetchSignStatus() }
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
                offerRepository.chooseStartDate(id, date)
            }
            if (response.isFailure || response.getOrNull()?.hasErrors() == true) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.let {
                offerRepository.writeStartDateToCache(quoteIds, it)
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
            if (response.isFailure || response.getOrNull()?.hasErrors() == true) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }
            response.getOrNull()?.data?.let { offerRepository.removeStartDateFromCache(quoteIds, it) }
        }
    }
}
