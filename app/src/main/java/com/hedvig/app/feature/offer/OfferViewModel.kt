package com.hedvig.app.feature.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.type.QuoteBundleAppConfigurationTitle
import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.adyen.AdyenRepository
import com.hedvig.app.feature.chat.usecase.TriggerFreeTextChatUseCase
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
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.SubscribeToDataCollectionStatusUseCase.Status.Content
import com.hedvig.app.feature.offer.usecase.providerstatus.GetProviderDisplayNameUseCase
import com.hedvig.app.feature.perils.PerilItem
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import e
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
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

        object OpenChat : Event()
        object Error : Event()

        data class ApproveError(
            val postSignScreen: PostSignScreen,
        ) : Event()

        data class ApproveSuccessful(
            val startDate: LocalDate?,
            val postSignScreen: PostSignScreen,
            val bundleDisplayName: String,
        ) : Event()

        data class StartSwedishBankIdSign(val autoStartToken: String) : Event()

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
            val paymentMethods: PaymentMethodsApiResponse?,
        ) : ViewState()
    }

    protected sealed class OfferAndLoginStatus {
        object Loading : OfferAndLoginStatus()
        object Error : OfferAndLoginStatus()
        data class Content(
            val offerData: OfferQuery.Data,
            val loginStatus: LoginStatus,
            val paymentMethods: PaymentMethodsApiResponse?,
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
    private val getProviderDisplayNameUseCase: GetProviderDisplayNameUseCase,
    private val tracker: OfferTracker,
    private val adyenRepository: AdyenRepository,
    private val marketManager: MarketManager,
    private val freeTextChatUseCase: TriggerFreeTextChatUseCase
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

    override val viewState: StateFlow<ViewState> = offerAndLoginStatus.transformLatest { offerResponse ->
        when (offerResponse) {
            OfferAndLoginStatus.Error -> emit(ViewState.Error)
            OfferAndLoginStatus.Loading -> emit(ViewState.Loading)
            is OfferAndLoginStatus.Content -> {
                // When we do more than one insurance comparison we will want to get all the dataCollectionIds instead.
                val insurelyDataCollectionReferenceUuid =
                    offerResponse.offerData.quoteBundle.quotes.firstNotNullOfOrNull(OfferQuery.Quote::dataCollectionId)
                if (insurelyDataCollectionReferenceUuid == null) {
                    emit(
                        produceViewState(
                            data = offerResponse.offerData,
                            loginStatus = offerResponse.loginStatus,
                            paymentMethods = offerResponse.paymentMethods,
                        )
                    )
                } else {
                    subscribeToDataCollectionStatusUseCase.invoke(insurelyDataCollectionReferenceUuid)
                        .collectLatest { dataCollectionStatus ->
                            coroutineScope {
                                val dataCollectionResult = async {
                                    getDataCollectionResultUseCase
                                        .invoke(insurelyDataCollectionReferenceUuid)
                                        .let { result ->
                                            (result as? GetDataCollectionResultUseCase.Result.Success)?.data
                                        }
                                }
                                val insuranceProviderDisplayName = async {
                                    if (dataCollectionStatus is Content) {
                                        val insuranceCompany =
                                            dataCollectionStatus.dataCollectionStatus.insuranceCompany
                                        getProviderDisplayNameUseCase.invoke(insuranceCompany)
                                    } else {
                                        null
                                    }
                                }
                                emit(
                                    produceViewState(
                                        offerResponse.offerData,
                                        offerResponse.loginStatus,
                                        offerResponse.paymentMethods,
                                        dataCollectionStatus,
                                        dataCollectionResult.await(),
                                        insuranceProviderDisplayName.await()
                                    )
                                )
                            }
                        }
                }
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

                                val paymentMethods = if (marketManager.market == Market.NO) {
                                    adyenRepository.paymentMethods()
                                        .data
                                        ?.availablePaymentMethods
                                        ?.paymentMethodsResponse
                                } else {
                                    null
                                }

                                offerAndLoginStatus.value = OfferAndLoginStatus.Content(
                                    response.data,
                                    loginStatus,
                                    paymentMethods
                                )
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
        tracker.viewOffer(
            data.quoteBundle.quotes.map { it.typeOfContract.rawValue },
            data.quoteBundle.appConfiguration.postSignStep.name,
        )
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
        paymentMethods: PaymentMethodsApiResponse?,
        dataCollectionStatus: SubscribeToDataCollectionStatusUseCase.Status? = null,
        dataCollectionResult: DataCollectionResult? = null,
        insuranceProviderDisplayName: String? = null,
    ): ViewState {
        val topOfferItems = OfferItemsBuilder.createTopOfferItems(
            data,
            dataCollectionStatus,
            dataCollectionResult,
            insuranceProviderDisplayName,
            paymentMethods
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
            paymentMethods = paymentMethods
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
        val event = when (freeTextChatUseCase.invoke()) {
            is Either.Left -> Event.Error
            is Either.Right -> Event.OpenChat
        }
        _events.trySend(event)
    }

    override fun onOpenQuoteDetails(id: String) {
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
                    Event.StartSwedishBankIdSign(result.autoStartToken)
                )
                SignQuotesUseCase.SignQuoteResult.Success -> offerAndLoginStatus.value = OfferAndLoginStatus.Error
            }
        }
    }
}
