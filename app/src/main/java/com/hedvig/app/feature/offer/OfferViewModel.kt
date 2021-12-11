package com.hedvig.app.feature.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationTitle
import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.offer.quotedetail.buildDocuments
import com.hedvig.app.feature.offer.quotedetail.buildInsurableLimits
import com.hedvig.app.feature.offer.quotedetail.buildPerils
import com.hedvig.app.feature.offer.ui.CheckoutLabel
import com.hedvig.app.feature.offer.ui.OfferModel
import com.hedvig.app.feature.offer.ui.checkout.ApproveQuotesUseCase
import com.hedvig.app.feature.offer.ui.checkout.CheckoutParameter
import com.hedvig.app.feature.offer.ui.checkout.SignQuotesUseCase
import com.hedvig.app.feature.offer.ui.checkoutLabel
import com.hedvig.app.feature.offer.usecase.GetPostSignDependenciesUseCase
import com.hedvig.app.feature.offer.usecase.GetQuoteUseCase
import com.hedvig.app.feature.offer.usecase.GetQuotesUseCase
import com.hedvig.app.feature.offer.usecase.RefreshQuotesUseCase
import com.hedvig.app.feature.offer.usecase.datacollectionresult.DataCollectionResult
import com.hedvig.app.feature.offer.usecase.datacollectionresult.GetDataCollectionResultUseCase
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.SubscribeToDataCollectionStatusUseCase
import com.hedvig.app.feature.perils.PerilItem
import e
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

abstract class OfferViewModel : ViewModel() {
    abstract val viewState: StateFlow<ViewState>

    sealed class Event {
        data class OpenQuoteDetails(
            val quoteDetailItems: QuoteDetailItems,
        ) : Event()

        data class OpenCheckout(
            val checkoutParameter: CheckoutParameter,
        ) : Event()

        data class ApproveError(
            val postSignScreen: PostSignScreen,
        ) : Event()

        data class ApproveSuccessful(
            val startDate: LocalDate?,
            val postSignScreen: PostSignScreen,
            val bundleDisplayName: String,
        ) : Event()

        data class StartSwedishBankIdSign(val quoteIds: List<String>, val autoStartToken: String) : Event()

        object DiscardOffer : Event()
    }

    protected val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    abstract fun removeDiscount()
    abstract fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data)
    abstract suspend fun triggerOpenChat()

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

    sealed class ViewState {
        object Loading : ViewState()
        object Error : ViewState()
        data class Content(
            val topOfferItems: List<OfferModel> = emptyList(),
            val perils: List<PerilItem> = emptyList(),
            val documents: List<DocumentItems> = emptyList(),
            val insurableLimitsItems: List<InsurableLimitItem> = emptyList(),
            val bottomOfferItems: List<OfferModel> = emptyList(),
            val signMethod: SignMethod = SignMethod.SIMPLE_SIGN,
            val checkoutLabel: CheckoutLabel = CheckoutLabel.CONFIRM,
            val title: QuoteBundleAppConfigurationTitle = QuoteBundleAppConfigurationTitle.LOGO,
            val loginStatus: LoginStatus = LoginStatus.LOGGED_IN,
        ) : ViewState()
    }

    protected sealed class OfferAndLoginStatus {
        object Loading : OfferAndLoginStatus()
        object Error : OfferAndLoginStatus()
        data class Content(
            val offerData: OfferQuery.Data,
            val loginStatus: LoginStatus,
        ) : OfferAndLoginStatus()
    }

    abstract fun onOpenCheckout()
    abstract fun reload()
    abstract fun onDiscardOffer()
    abstract fun onGoToDirectDebit()
    abstract fun onSwedishBankIdSign()
}

class OfferViewModelImpl(
    private val _quoteIds: List<String>,
    private val offerRepository: OfferRepository,
    private val getQuotesUseCase: GetQuotesUseCase,
    private val getQuoteUseCase: GetQuoteUseCase,
    private val loginStatusService: LoginStatusService,
    private val approveQuotesUseCase: ApproveQuotesUseCase,
    private val refreshQuotesUseCase: RefreshQuotesUseCase,
    private val signQuotesUseCase: SignQuotesUseCase,
    shouldShowOnNextAppStart: Boolean,
    private val getPostSignDependenciesUseCase: GetPostSignDependenciesUseCase,
    subscribeToDataCollectionStatusUseCase: SubscribeToDataCollectionStatusUseCase,
    private val getDataCollectionResultUseCase: GetDataCollectionResultUseCase,
    private val tracker: OfferTracker,
) : OfferViewModel() {

    private lateinit var quoteIds: List<String>

    init {
        loginStatusService.isViewingOffer = shouldShowOnNextAppStart

        viewModelScope.launch {
            loadQuoteIds()
        }
    }

    private val offerAndLoginStatus: MutableStateFlow<OfferAndLoginStatus> =
        MutableStateFlow(OfferAndLoginStatus.Loading)

    private val dataCollectionStatus: Flow<SubscribeToDataCollectionStatusUseCase.Status?> =
        if (insurelyDataCollectionReferenceUuid != null) {
            subscribeToDataCollectionStatusUseCase.invoke(insurelyDataCollectionReferenceUuid)
        } else {
            flowOf(null)
        }

    override val viewState: StateFlow<ViewState> = combine(
        offerAndLoginStatus,
        dataCollectionStatus,
    ) { offerResponse, dataCollectionStatus ->
        return@combine when (offerResponse) {
            OfferAndLoginStatus.Error -> ViewState.Error
            OfferAndLoginStatus.Loading -> ViewState.Loading
            is OfferAndLoginStatus.Content -> {
                val dataCollectionResult =
                    if (
                        dataCollectionStatus != null &&
                        dataCollectionStatus !is SubscribeToDataCollectionStatusUseCase.Status.Error &&
                        insurelyDataCollectionReferenceUuid != null
                    ) {
                        getDataCollectionResultUseCase
                            .invoke(insurelyDataCollectionReferenceUuid)
                            .let { result ->
                                (result as? GetDataCollectionResultUseCase.Result.Success)?.data
                            }
                    } else {
                        null
                    }
                produceViewState(
                    offerResponse.offerData,
                    offerResponse.loginStatus,
                    dataCollectionStatus,
                    dataCollectionResult,
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ViewState.Loading,
        )

    private suspend fun loadQuoteIds() {
        when (val idsResult = getQuotesUseCase.invoke(_quoteIds)) {
            is GetQuotesUseCase.Result.Success -> {
                quoteIds = idsResult.ids
                idsResult
                    .data
                    .onEach { response ->
                        when (response) {
                            is OfferRepository.OfferResult.Error -> {
                                offerAndLoginStatus.value = OfferAndLoginStatus.Error
                            }
                            is OfferRepository.OfferResult.Success -> {
                                trackView(response.data)
                                val loginStatus = loginStatusService.getLoginStatus()
                                offerAndLoginStatus.value = OfferAndLoginStatus.Content(response.data, loginStatus)
                            }
                        }
                    }
                    .catch {
                        offerAndLoginStatus.value = OfferAndLoginStatus.Error
                    }
                    .launchIn(viewModelScope)
            }
            is GetQuotesUseCase.Result.Error -> {
                offerAndLoginStatus.value = OfferAndLoginStatus.Error
            }
        }
    }

    private var hasTrackedView = false
    private fun trackView(data: OfferQuery.Data) {
        if (hasTrackedView) {
            return
        }
        hasTrackedView = true
        tracker.viewOffer(data.quoteBundle.quotes.map { it.typeOfContract.rawValue })
    }

    override fun onOpenCheckout() {
        _events.trySend(Event.OpenCheckout(CheckoutParameter(quoteIds = _quoteIds)))
    }

    override fun approveOffer() {
        viewModelScope.launch {
            offerAndLoginStatus.value = OfferAndLoginStatus.Loading
            val postSignDependencies = getPostSignDependenciesUseCase.invoke(quoteIds)
            if (postSignDependencies !is GetPostSignDependenciesUseCase.Result.Success) {
                offerAndLoginStatus.value = OfferAndLoginStatus.Error
                return@launch
            }
            when (val result = approveQuotesUseCase.approveQuotesAndClearCache(quoteIds)) {
                is ApproveQuotesUseCase.ApproveQuotesResult.Error.GeneralError ->
                    offerAndLoginStatus.value =
                        OfferAndLoginStatus.Error
                ApproveQuotesUseCase.ApproveQuotesResult.Error.ApproveError -> _events.trySend(
                    Event.ApproveError(postSignDependencies.postSignScreen)
                )
                is ApproveQuotesUseCase.ApproveQuotesResult.Success -> _events.trySend(
                    Event.ApproveSuccessful(
                        result.date,
                        postSignDependencies.postSignScreen,
                        postSignDependencies.displayName
                    )
                )
            }
        }
    }

    private fun produceViewState(
        data: OfferQuery.Data,
        loginStatus: LoginStatus,
        dataCollectionStatus: SubscribeToDataCollectionStatusUseCase.Status?,
        dataCollectionResult: DataCollectionResult?,
    ): ViewState {
        val topOfferItems = OfferItemsBuilder.createTopOfferItems(
            data,
            dataCollectionStatus,
            dataCollectionResult
        )
        val perilItems = OfferItemsBuilder.createPerilItems(data.quoteBundle.quotes)
        val insurableLimitsItems = OfferItemsBuilder.createInsurableLimits(data.quoteBundle.quotes)
        val documentItems = OfferItemsBuilder.createDocumentItems(data.quoteBundle.quotes)
        val bottomOfferItems = OfferItemsBuilder.createBottomOfferItems(data)
        return ViewState.Content(
            topOfferItems = topOfferItems,
            perils = perilItems,
            documents = documentItems,
            insurableLimitsItems = insurableLimitsItems,
            bottomOfferItems = bottomOfferItems,
            signMethod = data.signMethodForQuotes,
            checkoutLabel = data.checkoutLabel(),
            title = data.quoteBundle.appConfiguration.title,
            loginStatus = loginStatus,
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

    override fun onOpenQuoteDetails(
        id: String,
    ) {
        viewModelScope.launch {
            when (val result = getQuoteUseCase(quoteIds, id)) {
                GetQuoteUseCase.Result.Error -> {
                    offerAndLoginStatus.value = OfferAndLoginStatus.Error
                }
                is GetQuoteUseCase.Result.Success -> {
                    _events.trySend(
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
        offerAndLoginStatus.value = OfferAndLoginStatus.Loading
        viewModelScope.launch {
            if (!::quoteIds.isInitialized) {
                loadQuoteIds()
            }
            when (refreshQuotesUseCase.invoke(quoteIds)) {
                RefreshQuotesUseCase.Result.Success -> {
                }
                is RefreshQuotesUseCase.Result.Error -> offerAndLoginStatus.value = OfferAndLoginStatus.Error
            }
        }
    }

    override fun onDiscardOffer() {
        loginStatusService.isViewingOffer = false
        _events.trySend(Event.DiscardOffer)
    }

    override fun onGoToDirectDebit() {
        loginStatusService.isViewingOffer = false
    }

    override fun onSwedishBankIdSign() {
        viewModelScope.launch {
            when (val result = signQuotesUseCase.signQuotesAndClearCache(quoteIds)) {
                is SignQuotesUseCase.SignQuoteResult.Error -> offerAndLoginStatus.value = OfferAndLoginStatus.Error
                is SignQuotesUseCase.SignQuoteResult.StartSwedishBankId -> _events.trySend(
                    Event.StartSwedishBankIdSign(quoteIds, result.autoStartToken)
                )
                SignQuotesUseCase.SignQuoteResult.Success -> offerAndLoginStatus.value = OfferAndLoginStatus.Error
            }
        }
    }
}
