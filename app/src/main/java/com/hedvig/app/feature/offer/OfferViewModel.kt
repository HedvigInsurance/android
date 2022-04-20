package com.hedvig.app.feature.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.computations.either
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.hedvig.app.R
import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.adyen.AdyenRepository
import com.hedvig.app.feature.adyen.PaymentTokenId
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.checkout.CheckoutParameter
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.feature.offer.model.QuoteBundleVariant
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.model.paymentApiResponseOrNull
import com.hedvig.app.feature.offer.model.quotebundle.PostSignScreen
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import com.hedvig.app.feature.offer.usecase.AddPaymentTokenUseCase
import com.hedvig.app.feature.offer.usecase.EditCampaignUseCase
import com.hedvig.app.feature.offer.usecase.ExternalProvider
import com.hedvig.app.feature.offer.usecase.GetBundleVariantUseCase
import com.hedvig.app.feature.offer.usecase.GetExternalInsuranceProviderUseCase
import com.hedvig.app.feature.offer.usecase.SignQuotesUseCase
import com.hedvig.app.feature.perils.PerilItem
import com.hedvig.app.util.ErrorMessage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
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

        object OpenChat : Event()

        data class ApproveError(
            val postSignScreen: PostSignScreen,
        ) : Event()

        data class ApproveSuccessful(
            val startDate: LocalDate?,
            val postSignScreen: PostSignScreen,
            val bundleDisplayName: String,
        ) : Event()

        data class StartSwedishBankIdSign(val autoStartToken: String?) : Event()

        object DiscardOffer : Event()
    }

    protected val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    abstract fun removeDiscount()
    abstract suspend fun triggerOpenChat()

    data class QuoteDetailItems(
        val displayName: String,
        val perils: List<PerilItem.Peril>,
        val insurableLimits: List<InsurableLimitItem.InsurableLimit>,
        val documents: List<DocumentItems.Document>,
    ) {
        constructor(quote: QuoteBundle.Quote) : this(
            quote.displayName,
            quote.perils.map { PerilItem.Peril(it) },
            quote.insurableLimits,
            quote.insuranceTerms
        )
    }

    abstract fun onOpenQuoteDetails(id: String)

    abstract fun approveOffer()

    sealed class ViewState {
        object Loading : ViewState()
        data class Error(val message: String? = null) : ViewState()
        data class Content(
            val offerModel: OfferModel,
            val bundleVariant: QuoteBundleVariant,
            val loginStatus: LoginStatus = LoginStatus.LoggedIn,
            val paymentMethods: PaymentMethodsApiResponse?,
            val externalProvider: ExternalProvider?,
            val onVariantSelected: (id: String) -> Unit,
        ) : ViewState() {
            fun createTopOfferItems() = OfferItemsBuilder.createTopOfferItems(
                quoteBundleVariant = bundleVariant,
                externalProvider = externalProvider,
                paymentMethods = paymentMethods,
                onVariantSelected = onVariantSelected,
                offerModel = offerModel,
            )

            fun createBottomOfferItems() = OfferItemsBuilder.createBottomOfferItems(
                bundleVariant = bundleVariant
            )

            fun createPerilItems() = if (bundleVariant.bundle.quotes.size == 1) {
                bundleVariant.bundle.quotes.first().perils.map { PerilItem.Peril(it) }
            } else {
                emptyList()
            }

            fun createDocumentItems() = if (bundleVariant.bundle.quotes.size == 1) {
                listOf(DocumentItems.Header(R.string.OFFER_DOCUMENTS_SECTION_TITLE)) +
                    bundleVariant.bundle.quotes.first().insuranceTerms
            } else {
                emptyList()
            }

            fun createInsurableLimitItems() = if (bundleVariant.bundle.quotes.size == 1) {
                listOf(InsurableLimitItem.Header.Details) +
                    bundleVariant.bundle.quotes.first().insurableLimits
            } else {
                emptyList()
            }
        }
    }

    abstract fun onOpenCheckout()
    abstract fun reload()
    abstract fun onDiscardOffer()
    abstract fun onGoToDirectDebit()
    abstract fun onSwedishBankIdSign()
    abstract fun onPaymentTokenIdReceived(id: PaymentTokenId)
}

class OfferViewModelImpl(
    private val quoteCartId: QuoteCartId,
    private val offerRepository: OfferRepository,
    private val loginStatusService: LoginStatusService,
    private val signQuotesUseCase: SignQuotesUseCase,
    shouldShowOnNextAppStart: Boolean,
    private val adyenRepository: AdyenRepository,
    private val chatRepository: ChatRepository,
    private val editCampaignUseCase: EditCampaignUseCase,
    private val addPaymentTokenUseCase: AddPaymentTokenUseCase,
    private val getExternalInsuranceProviderUseCase: GetExternalInsuranceProviderUseCase,
    private val getBundleVariantUseCase: GetBundleVariantUseCase,
) : OfferViewModel() {

    private val _viewState: MutableStateFlow<ViewState> = MutableStateFlow(ViewState.Loading)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val viewState: StateFlow<ViewState> = _viewState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ViewState.Loading,
        )

    init {
        loginStatusService.isViewingOffer = shouldShowOnNextAppStart
        loginStatusService.persistOfferIds(quoteCartId)

        getBundleVariantUseCase.bundleVariantFlow
            .flatMapLatest(::toViewState)
            .onEach { viewState: ViewState ->
                _viewState.value = viewState
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            offerRepository.queryAndEmitOffer(quoteCartId)
        }
    }

    private fun toViewState(offerResult: Either<ErrorMessage, Pair<OfferModel, QuoteBundleVariant>>): Flow<ViewState> =
        offerResult.fold(
            ifLeft = { flowOf(ViewState.Error(it.message)) },
            ifRight = { pair ->
                val offerModel = pair.first
                val bundle = pair.second
                if (bundle.externalProviderId != null) {
                    getExternalInsuranceProviderUseCase
                        .observeExternalProviderOrNull(bundle.externalProviderId)
                        .mapLatest { externalProvider ->
                            ViewState.Content(
                                offerModel = offerModel,
                                bundleVariant = bundle,
                                loginStatus = loginStatusService.getLoginStatus(),
                                paymentMethods = offerModel.paymentApiResponseOrNull()
                                    ?: adyenRepository.paymentMethodsResponse(),
                                externalProvider = externalProvider,
                                onVariantSelected = { variantId ->
                                    getBundleVariantUseCase.invoke(variantId)
                                }
                            )
                        }
                } else {
                    flow {
                        emit(
                            ViewState.Content(
                                offerModel = offerModel,
                                bundleVariant = bundle,
                                loginStatus = loginStatusService.getLoginStatus(),
                                paymentMethods = offerModel.paymentApiResponseOrNull()
                                    ?: adyenRepository.paymentMethodsResponse(),
                                externalProvider = null,
                                onVariantSelected = { variantId ->
                                    getBundleVariantUseCase.invoke(variantId)
                                }
                            )
                        )
                    }
                }
            }
        )

    override fun onOpenCheckout() {
        viewModelScope.launch {
            getBundleVariantUseCase.bundleVariantFlow
                .first()
                .map { it.second.bundle.quotes.map { it.id } }
                .fold(
                    ifLeft = { _viewState.value = ViewState.Error(it.message) },
                    ifRight = { quoteIds ->
                        val parameter = CheckoutParameter(quoteIds, quoteCartId)
                        val event = Event.OpenCheckout(parameter)
                        _events.trySend(event)
                    }
                )
        }
    }

    override fun removeDiscount() {
        viewModelScope.launch {
            editCampaignUseCase.removeCampaignFromQuoteCart(quoteCartId)
                .tapLeft { _viewState.value = ViewState.Error(null) }
        }
    }

    override suspend fun triggerOpenChat() {
        chatRepository.triggerFreeTextChat().fold(
            ifLeft = { _viewState.value = ViewState.Error(null) },
            ifRight = { _events.trySend(Event.OpenChat) }
        )
    }

    override fun onOpenQuoteDetails(id: String) {
        viewModelScope.launch {
            getBundleVariantUseCase.bundleVariantFlow.first().map {
                it.second.bundle.quotes.first { it.id == id }
            }.fold(
                ifLeft = { _viewState.value = ViewState.Error(it.message) },
                ifRight = { quote ->
                    val quoteDetailItems = QuoteDetailItems(quote)
                    val event = Event.OpenQuoteDetails(quoteDetailItems)
                    _events.trySend(event)
                }
            )
        }
    }

    override fun reload() {
        _viewState.value = ViewState.Loading
        viewModelScope.launch {
            offerRepository.queryAndEmitOffer(quoteCartId)
        }
    }

    override fun onDiscardOffer() {
        loginStatusService.isViewingOffer = false
        _events.trySend(Event.DiscardOffer)
    }

    override fun onGoToDirectDebit() {
        loginStatusService.isViewingOffer = false
    }

    override fun approveOffer() {
        getQuoteIdsAndStartSign {
            offerRepository.queryAndEmitOffer(quoteCartId)
        }
    }

    override fun onSwedishBankIdSign() {
        getQuoteIdsAndStartSign(::handleSignQuoteResult)
    }

    private fun getQuoteIdsAndStartSign(onComplete: suspend (SignQuotesUseCase.SignQuoteResult) -> Unit) {
        viewModelScope.launch {
            either<ErrorMessage, SignQuotesUseCase.SignQuoteResult> {
                val quoteIds = getBundleVariantUseCase.bundleVariantFlow
                    .first()
                    .map { it.second.bundle.quotes.map { it.id } }
                    .bind()

                signQuotesUseCase.signQuotesAndClearCache(quoteIds, quoteCartId).bind()
            }.fold(
                ifLeft = { _viewState.value = ViewState.Error(it.message) },
                ifRight = { result -> onComplete(result) }
            )
        }
    }

    private fun handleSignQuoteResult(result: SignQuotesUseCase.SignQuoteResult) {
        when (result) {
            is SignQuotesUseCase.SignQuoteResult.StartSwedishBankId -> {
                _events.trySend(Event.StartSwedishBankIdSign(result.autoStartToken))
            }
            SignQuotesUseCase.SignQuoteResult.StartSimpleSign -> {
                _viewState.value = ViewState.Error("Invalid offer state")
            }
        }
    }

    override fun onPaymentTokenIdReceived(id: PaymentTokenId) {
        viewModelScope.launch {
            addPaymentTokenUseCase.invoke(quoteCartId, id)
                .tapLeft { _viewState.value = ViewState.Error(null) }
            onOpenCheckout()
        }
    }
}
