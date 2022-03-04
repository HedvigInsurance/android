package com.hedvig.app.feature.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.R
import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.adyen.AdyenRepository
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.checkout.ApproveQuotesUseCase
import com.hedvig.app.feature.checkout.CheckoutParameter
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.feature.offer.model.quotebundle.CheckoutMethod
import com.hedvig.app.feature.offer.model.quotebundle.PostSignScreen
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import com.hedvig.app.feature.offer.model.quotebundle.ViewConfiguration
import com.hedvig.app.feature.offer.ui.CheckoutLabel
import com.hedvig.app.feature.offer.ui.OfferItems
import com.hedvig.app.feature.offer.usecase.GetPostSignDependenciesUseCase
import com.hedvig.app.feature.offer.usecase.RefreshQuotesUseCase
import com.hedvig.app.feature.offer.usecase.SignQuotesUseCase
import com.hedvig.app.feature.offer.usecase.datacollectionresult.DataCollectionResult
import com.hedvig.app.feature.offer.usecase.datacollectionresult.GetDataCollectionResultUseCase
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.SubscribeToDataCollectionStatusUseCase
import com.hedvig.app.feature.offer.usecase.datacollectionstatus.SubscribeToDataCollectionStatusUseCase.Status.Content
import com.hedvig.app.feature.offer.usecase.getquote.GetQuoteIdsUseCase
import com.hedvig.app.feature.offer.usecase.getquote.GetQuoteUseCase
import com.hedvig.app.feature.offer.usecase.getquote.GetQuotesUseCase
import com.hedvig.app.feature.offer.usecase.providerstatus.GetProviderDisplayNameUseCase
import com.hedvig.app.feature.perils.PerilItem
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.hanalytics.HAnalytics
import e
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
            val topOfferItems: List<OfferItems> = emptyList(),
            val perils: List<PerilItem> = emptyList(),
            val documents: List<DocumentItems> = emptyList(),
            val insurableLimitsItems: List<InsurableLimitItem> = emptyList(),
            val bottomOfferItems: List<OfferItems> = emptyList(),
            val checkoutMethod: CheckoutMethod = CheckoutMethod.SIMPLE_SIGN,
            val checkoutLabel: CheckoutLabel = CheckoutLabel.CONFIRM,
            val title: ViewConfiguration.Title = ViewConfiguration.Title.LOGO,
            val loginStatus: LoginStatus = LoginStatus.LoggedIn,
            val paymentMethods: PaymentMethodsApiResponse?,
        ) : ViewState()
    }

    protected sealed class OfferAndLoginStatus {
        object Loading : OfferAndLoginStatus()
        object Error : OfferAndLoginStatus()
        data class Content(
            val offerResult: OfferModel,
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
    private var quoteIds: List<String>,
    private val quoteCartId: String?,
    private val offerRepository: OfferRepository,
    private val getQuotesUseCase: GetQuotesUseCase,
    private val getQuoteIdsUseCase: GetQuoteIdsUseCase,
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
    private val adyenRepository: AdyenRepository,
    private val marketManager: MarketManager,
    private val chatRepository: ChatRepository,
    private val hAnalytics: HAnalytics,
) : OfferViewModel() {

    init {
        loginStatusService.isViewingOffer = shouldShowOnNextAppStart
        loginStatusService.persistOfferIds(quoteCartId, quoteIds)

        viewModelScope.launch {
            loadQuoteIds()
            loadQuotes(quoteIds)
        }
    }

    private val offerAndLoginStatus: MutableStateFlow<OfferAndLoginStatus> =
        MutableStateFlow(OfferAndLoginStatus.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val viewState: StateFlow<ViewState> = offerAndLoginStatus.transformLatest { offerResponse ->
        when (offerResponse) {
            OfferAndLoginStatus.Error -> emit(ViewState.Error)
            OfferAndLoginStatus.Loading -> emit(ViewState.Loading)
            is OfferAndLoginStatus.Content -> {
                // When we do more than one insurance comparison we will want to get all the dataCollectionIds instead.
                val insurelyDataCollectionReferenceUuid = offerResponse.offerResult
                    .quoteBundle
                    .quotes
                    .firstNotNullOfOrNull(QuoteBundle.Quote::dataCollectionId)

                if (insurelyDataCollectionReferenceUuid == null) {
                    emit(
                        produceViewState(
                            data = offerResponse.offerResult,
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
                                        offerResponse.offerResult,
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

    private suspend fun loadQuoteIds() = getQuoteIdsUseCase.invoke(quoteCartId)
        .map { it.ids }
        .fold(
            ifLeft = { offerAndLoginStatus.value = OfferAndLoginStatus.Error },
            ifRight = {
                hAnalytics.screenViewOffer(it)
                quoteIds = it
            }
        )

    private fun loadQuotes(quoteIds: List<String>) {
        getQuotesUseCase.invoke(quoteIds, quoteCartId).onEach { result ->
            result.fold(
                ifLeft = { offerAndLoginStatus.value = OfferAndLoginStatus.Error },
                ifRight = {
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
                        it,
                        loginStatus,
                        paymentMethods
                    )
                }
            )
        }.launchIn(viewModelScope)
    }

    override fun onOpenCheckout() {
        _events.trySend(
            Event.OpenCheckout(
                CheckoutParameter(
                    quoteIds = quoteIds,
                    quoteCartId = quoteCartId
                )
            )
        )
    }

    override fun approveOffer() {
        viewModelScope.launch {
            offerAndLoginStatus.value = OfferAndLoginStatus.Loading
            getPostSignDependenciesUseCase.invoke(quoteIds).fold(
                ifLeft = { offerAndLoginStatus.value = OfferAndLoginStatus.Error },
                ifRight = { approveQuotes(it) }
            )
        }
    }

    private suspend fun approveQuotes(postSignResult: GetPostSignDependenciesUseCase.Result) {
        approveQuotesUseCase.approveQuotesAndClearCache(quoteIds)
            .mapLeft { handleApproveError(it, postSignResult) }
            .map { date ->
                hAnalytics.quotesSigned(quoteIds)
                loginStatusService.isViewingOffer = false
                _events.trySend(
                    Event.ApproveSuccessful(
                        date,
                        postSignResult.postSignScreen,
                        postSignResult.displayName
                    )
                )
            }
    }

    private fun handleApproveError(
        error: ApproveQuotesUseCase.Error,
        postSignResult: GetPostSignDependenciesUseCase.Result
    ) {
        when (error) {
            ApproveQuotesUseCase.Error.ApproveError -> {
                _events.trySend(Event.ApproveError(postSignResult.postSignScreen))
            }
            is ApproveQuotesUseCase.Error.GeneralError -> {
                offerAndLoginStatus.value = OfferAndLoginStatus.Error
            }
        }
    }

    private fun produceViewState(
        data: OfferModel,
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

        val bottomOfferItems = OfferItemsBuilder.createBottomOfferItems(data)

        return ViewState.Content(
            topOfferItems = topOfferItems,
            perils = if (data.quoteBundle.quotes.size == 1) {
                data.quoteBundle.quotes.first().perils.map { PerilItem.Peril(it) }
            } else {
                emptyList()
            },
            documents = if (data.quoteBundle.quotes.size == 1) {
                listOf(DocumentItems.Header(R.string.OFFER_DOCUMENTS_SECTION_TITLE)) +
                    data.quoteBundle.quotes.first().insuranceTerms
            } else {
                emptyList()
            },
            insurableLimitsItems = if (data.quoteBundle.quotes.size == 1) {
                listOf(InsurableLimitItem.Header.Details) +
                    data.quoteBundle.quotes.first().insurableLimits
            } else {
                emptyList()
            },
            bottomOfferItems = bottomOfferItems,
            checkoutLabel = data.checkoutLabel,
            title = data.quoteBundle.viewConfiguration.title,
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
        chatRepository.triggerFreeTextChat()
            .fold(
                ifLeft = { Event.Error },
                ifRight = { Event.OpenChat }
            )
            .let(_events::trySend)
    }

    override fun onOpenQuoteDetails(id: String) {
        viewModelScope.launch {
            getQuoteUseCase.invoke(quoteIds, id)
                .mapLeft { offerAndLoginStatus.value = OfferAndLoginStatus.Error }
                .map {
                    _events.trySend(
                        Event.OpenQuoteDetails(
                            QuoteDetailItems(
                                it.displayName,
                                it.perils.map { PerilItem.Peril(it) },
                                it.insurableLimits,
                                it.insuranceTerms
                            )
                        )
                    )
                }
        }
    }

    override fun reload() {
        offerAndLoginStatus.value = OfferAndLoginStatus.Loading
        viewModelScope.launch {
            if (quoteIds.isEmpty()) {
                loadQuoteIds()
            }

            when (refreshQuotesUseCase.invoke(quoteIds)) {
                RefreshQuotesUseCase.Result.Success -> loadQuotes(quoteIds)
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
            when (val result = signQuotesUseCase.signQuotesAndClearCache(quoteIds, quoteCartId)) {
                is SignQuotesUseCase.SignQuoteResult.Error -> offerAndLoginStatus.value = OfferAndLoginStatus.Error
                is SignQuotesUseCase.SignQuoteResult.StartSwedishBankId -> _events.trySend(
                    Event.StartSwedishBankIdSign(result.autoStartToken)
                )
                SignQuotesUseCase.SignQuoteResult.Success -> offerAndLoginStatus.value = OfferAndLoginStatus.Error
            }
        }
    }
}
