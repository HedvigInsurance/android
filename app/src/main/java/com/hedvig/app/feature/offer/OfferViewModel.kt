package com.hedvig.app.feature.offer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationTitle
import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.android.owldroid.type.SignState
import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.offer.quotedetail.buildDocuments
import com.hedvig.app.feature.offer.quotedetail.buildInsurableLimits
import com.hedvig.app.feature.offer.quotedetail.buildPerils
import com.hedvig.app.feature.offer.ui.OfferModel
import com.hedvig.app.feature.offer.ui.checkout.ApproveQuotesUseCase
import com.hedvig.app.feature.offer.ui.checkout.CheckoutParameter
import com.hedvig.app.feature.offer.usecase.GetQuoteUseCase
import com.hedvig.app.feature.offer.usecase.GetQuotesUseCase
import com.hedvig.app.feature.offer.usecase.RefreshQuotesUseCase
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
import java.time.LocalDate

abstract class OfferViewModel : ViewModel() {
    protected val _viewState = MutableStateFlow(ViewState(isLoading = true))
    val viewState: StateFlow<ViewState> = _viewState

    sealed class Event {
        data class Error(val message: String? = null) : Event()

        object HasContracts : Event()
        data class OpenQuoteDetails(
            val quoteDetailItems: QuoteDetailItems,
        ) : Event()

        data class OpenCheckout(
            val checkoutParameter: CheckoutParameter
        ) : Event()

        object ApproveError : Event()
        data class ApproveSuccessful(
            val moveDate: LocalDate?
        ) : Event()

        object DiscardOffer : Event()
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
    abstract suspend fun triggerOpenChat()
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

    abstract fun approveOffer()

    data class ViewState(
        val topOfferItems: List<OfferModel> = emptyList(),
        val perils: List<PerilItem> = emptyList(),
        val documents: List<DocumentItems> = emptyList(),
        val insurableLimitsItems: List<InsurableLimitItem> = emptyList(),
        val bottomOfferItems: List<OfferModel> = emptyList(),
        val signMethod: SignMethod = SignMethod.SIMPLE_SIGN,
        val title: QuoteBundleAppConfigurationTitle = QuoteBundleAppConfigurationTitle.LOGO,
        val loginStatus: LoginStatus = LoginStatus.LOGGED_IN,
        val isLoading: Boolean = false
    )

    abstract fun onOpenCheckout()
    abstract fun reload()
    abstract fun onDiscardOffer()
    abstract fun onGoToDirectDebit()
}

class OfferViewModelImpl(
    private val _quoteIds: List<String>,
    private val offerRepository: OfferRepository,
    private val getQuotesUseCase: GetQuotesUseCase,
    private val getQuoteUseCase: GetQuoteUseCase,
    private val loginStatusService: LoginStatusService,
    private val approveQuotesUseCase: ApproveQuotesUseCase,
    private val refreshQuotesUseCase: RefreshQuotesUseCase,
    private val tracker: OfferTracker,
    shouldShowOnNextAppStart: Boolean
) : OfferViewModel() {

    private lateinit var quoteIds: List<String>

    override val autoStartToken = MutableLiveData<SignOfferMutation.Data>()
    override val signStatus = MutableLiveData<SignStatusFragment>()
    override val signError = MutableLiveData<Boolean>()

    init {
        loginStatusService.isViewingOffer = shouldShowOnNextAppStart

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
                                    _events.tryEmit(Event.Error())
                                }
                                is OfferRepository.OfferResult.Success -> {
                                    val loginStatus = loginStatusService.getLoginStatus()
                                    _viewState.value = toViewState(response.data, loginStatus)
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

    override fun onOpenCheckout() {
        _events.tryEmit(
            Event.OpenCheckout(
                CheckoutParameter(
                    quoteIds = _quoteIds
                )
            )
        )
    }

    override fun approveOffer() {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true)
            val event = when (val result = approveQuotesUseCase.approveQuotes(quoteIds)) {
                is ApproveQuotesUseCase.ApproveQuotesResult.Error.GeneralError -> Event.Error(result.message)
                ApproveQuotesUseCase.ApproveQuotesResult.Error.ApproveError -> Event.ApproveError
                is ApproveQuotesUseCase.ApproveQuotesResult.Success -> Event.ApproveSuccessful(result.date)
            }
            _events.tryEmit(event)
        }
    }

    private fun toViewState(data: OfferQuery.Data, loginStatus: LoginStatus): ViewState {
        val topOfferItems = OfferItemsBuilder.createTopOfferItems(data)
        val perilItems = OfferItemsBuilder.createPerilItems(data.quoteBundle.quotes)
        val insurableLimitsItems = OfferItemsBuilder.createInsurableLimits(data.quoteBundle.quotes)
        val documentItems = OfferItemsBuilder.createDocumentItems(data.quoteBundle.quotes)
        val bottomOfferItems = OfferItemsBuilder.createBottomOfferItems(data)
        return ViewState(
            topOfferItems = topOfferItems,
            perils = perilItems,
            documents = documentItems,
            insurableLimitsItems = insurableLimitsItems,
            bottomOfferItems = bottomOfferItems,
            signMethod = data.signMethodForQuotes,
            title = data.quoteBundle.appConfiguration.title,
            loginStatus = loginStatus
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

    override suspend fun triggerOpenChat() {
        val result = runCatching { offerRepository.triggerOpenChatFromOffer() }
        if (result.isFailure) {
            result.exceptionOrNull()?.let { e(it) }
        }
    }

    override fun startSign() {
        viewModelScope.launch {
            var hasCompletedSign = false
            offerRepository.subscribeSignStatus()
                .onEach { response ->
                    if (
                        response
                            .data
                            ?.signStatus
                            ?.status
                            ?.fragments
                            ?.signStatusFragment
                            ?.signState == SignState.COMPLETED &&
                        !hasCompletedSign
                    ) {
                        hasCompletedSign = true
                        tracker.signQuotes(quoteIds)
                    }
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

    override fun reload() {
        _viewState.value = _viewState.value.copy(isLoading = true)
        viewModelScope.launch {
            when (val result = refreshQuotesUseCase(quoteIds)) {
                RefreshQuotesUseCase.Result.Success -> {
                }
                is RefreshQuotesUseCase.Result.Error -> _events.tryEmit(Event.Error(result.message))
            }
        }
    }

    override fun onDiscardOffer() {
        loginStatusService.isViewingOffer = false
        _events.tryEmit(Event.DiscardOffer)
    }

    override fun onGoToDirectDebit() {
        loginStatusService.isViewingOffer = false
    }
}
