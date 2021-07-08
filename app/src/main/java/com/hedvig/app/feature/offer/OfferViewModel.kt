package com.hedvig.app.feature.offer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.offer.quotedetail.buildDocuments
import com.hedvig.app.feature.offer.quotedetail.buildInsurableLimits
import com.hedvig.app.feature.offer.quotedetail.buildPerils
import com.hedvig.app.feature.offer.ui.OfferModel
import com.hedvig.app.feature.offer.usecase.GetQuoteUseCase
import com.hedvig.app.feature.offer.usecase.GetQuotesUseCase
import com.hedvig.app.feature.perils.PerilItem
import e
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class OfferViewModel : ViewModel() {
    protected val _viewState = MutableStateFlow<ViewState>(ViewState.Loading(OfferItemsBuilder.createLoadingItem()))
    val viewState: StateFlow<ViewState> = _viewState

    sealed class Event {
        data class Error(val message: String? = null) : Event()

        object HasContracts : Event()
        data class OpenQuoteDetails(
            val quoteDetailItems: QuoteDetailItems,
        ) : Event()
    }

    protected val _events = MutableSharedFlow<Event>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<Event> = _events

    abstract val autoStartToken: LiveData<SignOfferMutation.Data>
    abstract val signStatus: LiveData<SignStatusFragment>
    abstract val signError: LiveData<Boolean>
    abstract fun removeDiscount()
    abstract fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data)
    abstract fun triggerOpenChat(done: () -> Unit)
    abstract fun startSign()
    abstract fun clearPreviousErrors()
    abstract fun manuallyRecheckSignStatus()

    data class QuoteDetailItems(
        val displayName: String,
        val perils: List<PerilItem.Peril>,
        val insurableLimits: List<InsurableLimitItem.InsurableLimit>,
        val documents: List<DocumentItems.Document>,
    )

    abstract fun onOpenQuoteDetails(
        id: String,
    )

    sealed class ViewState {
        data class Loaded(
            val topOfferItems: List<OfferModel>,
            val perils: List<PerilItem>,
            val documents: List<DocumentItems>,
            val insurableLimitsItems: List<InsurableLimitItem>,
            val bottomOfferItems: List<OfferModel>,
            val signMethod: SignMethod
        ) : ViewState()

        data class Loading(val loadingItem: List<OfferModel.Loading>) : ViewState()
    }
}

class OfferViewModelImpl(
    _quoteIds: List<String>,
    private val offerRepository: OfferRepository,
    private val getQuotesUseCase: GetQuotesUseCase,
    private val getQuoteUseCase: GetQuoteUseCase
) : OfferViewModel() {

    private lateinit var quoteIds: List<String>

    override val autoStartToken = MutableLiveData<SignOfferMutation.Data>()
    override val signStatus = MutableLiveData<SignStatusFragment>()
    override val signError = MutableLiveData<Boolean>()

    init {
        viewModelScope.launch {
            when (val idsResult = getQuotesUseCase(_quoteIds)) {
                is GetQuotesUseCase.Result.Success -> {
                    quoteIds = idsResult.ids
                    idsResult
                        .data
                        .onEach { response ->
                            when (response) {
                                is OfferRepository.OfferResult.Error -> {
                                    _events.tryEmit(Event.Error(response.message))
                                }
                                OfferRepository.OfferResult.HasContracts -> {
                                    _events.tryEmit(Event.HasContracts)
                                }
                                is OfferRepository.OfferResult.Success -> {
                                    _viewState.value = toViewState(response.data)
                                }
                            }
                        }
                        .catch {
                            _events.tryEmit(Event.Error(it.message))
                        }
                        .launchIn(this)
                }
                is GetQuotesUseCase.Result.Error -> {
                    _events.tryEmit(Event.Error(idsResult.message))
                }
            }
        }
    }

    private fun toViewState(data: OfferQuery.Data): ViewState {
        val topOfferItems = OfferItemsBuilder.createTopOfferItems(data)
        val perilItems = OfferItemsBuilder.createPerilItems(data.quoteBundle.quotes)
        val insurableLimitsItems = OfferItemsBuilder.createInsurableLimits(data.quoteBundle.quotes)
        val documentItems = OfferItemsBuilder.createDocumentItems(data.quoteBundle.quotes)
        val bottomOfferItems = OfferItemsBuilder.createBottomOfferItems(data.quoteBundle)
        return ViewState.Loaded(
            topOfferItems,
            perilItems,
            documentItems,
            insurableLimitsItems,
            bottomOfferItems,
            data.signMethodForQuotes
        )
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

    override fun onOpenQuoteDetails(
        id: String,
    ) {
        viewModelScope.launch {
            when (val result = getQuoteUseCase(quoteIds, id)) {
                GetQuoteUseCase.Result.Error -> {
                    _events.tryEmit(Event.Error())
                }
                is GetQuoteUseCase.Result.Success -> {
                    _events.tryEmit(
                        Event.OpenQuoteDetails(
                            QuoteDetailItems(
                                result.quote.displayName,
                                buildPerils(result.quote),
                                buildInsurableLimits(result.quote),
                                buildDocuments(result.quote)
                            )
                        )
                    )
                }
            }
        }
    }
}
