package com.hedvig.app.feature.offer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.continuations.ensureNotNull
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.app.authenticate.LoginStatus
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.adyen.PaymentTokenId
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.checkout.CheckoutParameter
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.embark.util.SelectedContractType
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.offer.model.Checkout
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.feature.offer.model.QuoteBundleVariant
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.model.quotebundle.OfferStartDate
import com.hedvig.app.feature.offer.model.quotebundle.PostSignScreen
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import com.hedvig.app.feature.offer.usecase.AddPaymentTokenUseCase
import com.hedvig.app.feature.offer.usecase.EditCampaignUseCase
import com.hedvig.app.feature.offer.usecase.GetQuoteCartCheckoutUseCase
import com.hedvig.app.feature.offer.usecase.ObserveOfferStateUseCase
import com.hedvig.app.feature.offer.usecase.OfferState
import com.hedvig.app.feature.offer.usecase.StartCheckoutUseCase
import com.hedvig.app.feature.perils.PerilItem
import com.hedvig.app.util.ErrorMessage
import com.hedvig.hanalytics.PaymentType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
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
      val payinType: PaymentType,
    ) : Event()

    object StartSwedishBankIdSign : Event()

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
      quote.insuranceTerms,
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
      val onVariantSelected: (id: String) -> Unit,
    ) : ViewState() {
      fun createTopOfferItems() = OfferItemsBuilder.createTopOfferItems(
        quoteBundleVariant = bundleVariant,
        paymentMethods = paymentMethods,
        onVariantSelected = onVariantSelected,
        offerModel = offerModel,
      )

      fun createBottomOfferItems() = OfferItemsBuilder.createBottomOfferItems(
        bundleVariant = bundleVariant,
      )

      fun createPerilItems() = if (bundleVariant.bundle.quotes.size == 1) {
        bundleVariant.bundle.quotes.first().perils.map { PerilItem.Peril(it) }
      } else {
        emptyList()
      }

      fun createDocumentItems() = if (bundleVariant.bundle.quotes.size == 1) {
        listOf(DocumentItems.Header(hedvig.resources.R.string.OFFER_DOCUMENTS_SECTION_TITLE)) +
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
  selectedContractTypes: List<SelectedContractType>,
  private val offerRepository: OfferRepository,
  private val loginStatusService: LoginStatusService,
  private val startCheckoutUseCase: StartCheckoutUseCase,
  shouldShowOnNextAppStart: Boolean,
  private val chatRepository: ChatRepository,
  private val editCampaignUseCase: EditCampaignUseCase,
  private val featureManager: FeatureManager,
  private val addPaymentTokenUseCase: AddPaymentTokenUseCase,
  getBundleVariantUseCase: ObserveOfferStateUseCase,
  private val selectedVariantStore: SelectedVariantStore,
  private val getQuoteCartCheckoutUseCase: GetQuoteCartCheckoutUseCase,
) : OfferViewModel() {

  private val _viewState: MutableStateFlow<ViewState> = MutableStateFlow(ViewState.Loading)
  override val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

  private val offerState: Flow<Either<ErrorMessage, OfferState>> =
    getBundleVariantUseCase.invoke(quoteCartId, selectedContractTypes)

  init {
    loginStatusService.isViewingOffer = shouldShowOnNextAppStart
    loginStatusService.persistOfferIds(quoteCartId)

    offerState
      .flatMapLatest(::toViewState)
      .onEach { viewState: ViewState ->
        _viewState.value = viewState
      }
      .launchIn(viewModelScope)
  }

  private fun toViewState(offerResult: Either<ErrorMessage, OfferState>): Flow<ViewState> =
    offerResult.fold(
      ifLeft = { flowOf(ViewState.Error(it.message)) },
      ifRight = { offerWithVariant: OfferState ->
        val offerModel = offerWithVariant.offerModel
        val bundle = offerWithVariant.selectedVariant
        flow {
          emit(
            ViewState.Content(
              offerModel = offerModel,
              bundleVariant = bundle,
              loginStatus = loginStatusService.getLoginStatus(),
              paymentMethods = offerModel.paymentMethodsApiResponse,
              onVariantSelected = { variantId ->
                selectedVariantStore.selectVariant(variantId)
              },
            ),
          )
        }
      },
    )

  override fun onOpenCheckout() {
    viewModelScope.launch {
      offerState
        .first()
        .map { it.selectedVariant.id }
        .fold(
          ifLeft = { _viewState.value = ViewState.Error(it.message) },
          ifRight = { selectedVariantId ->
            val parameter = CheckoutParameter(selectedVariantId, quoteCartId)
            val event = Event.OpenCheckout(parameter)
            _events.trySend(event)
          },
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
      ifRight = { _events.trySend(Event.OpenChat) },
    )
  }

  override fun onOpenQuoteDetails(id: String) {
    viewModelScope.launch {
      either {
        val currentOfferState = offerState.first().bind()
        val quote = ensureNotNull(currentOfferState.findQuote(id), ::ErrorMessage)
        val quoteDetailItems = QuoteDetailItems(quote)
        Event.OpenQuoteDetails(quoteDetailItems)
      }.fold(
        ifLeft = { _viewState.value = ViewState.Error(it.message) },
        ifRight = { event: Event ->
          _events.trySend(event)
        },
      )
    }
  }

  override fun reload() {
    _viewState.value = ViewState.Loading
    viewModelScope.launch {
      offerRepository.fetchNewOffer(quoteCartId)
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
      offerState
        .first()
        .map { it.selectedVariant }
        .fold(
          ifLeft = { _viewState.value = ViewState.Error(it.message) },
          ifRight = {
            viewModelScope.launch {
              featureManager.invalidateExperiments()
              val event = Event.ApproveSuccessful(
                startDate = (it.bundle.inception.startDate as? OfferStartDate.AtDate)?.date,
                postSignScreen = it.bundle.viewConfiguration.postSignScreen,
                bundleDisplayName = it.bundle.name,
                payinType = featureManager.getPaymentType(),
              )
              _events.send(event)
            }
          },
        )
    }
  }

  override fun onSwedishBankIdSign() {
    getQuoteIdsAndStartSign {
      offerRepository.fetchNewOffer(quoteCartId)
      _events.trySend(Event.StartSwedishBankIdSign)
    }
  }

  private fun getQuoteIdsAndStartSign(onComplete: suspend (StartCheckoutUseCase.Success) -> Unit) {
    viewModelScope.launch {
      either<ErrorMessage, StartCheckoutUseCase.Success> {
        val checkoutStatus = getQuoteCartCheckoutUseCase.invoke(quoteCartId)
          .mapLeft { ErrorMessage(it.message) }
          .bind()
          ?.status
        val isPending = checkoutStatus == Checkout.CheckoutStatus.PENDING

        if (isPending) {
          StartCheckoutUseCase.Success
        } else {
          val offer = offerState.first().bind()
          val quoteIds = offer.selectedQuoteIds
          startCheckoutUseCase.startCheckoutAndClearCache(quoteCartId, quoteIds).bind()
        }
      }.fold(
        ifLeft = { _viewState.value = ViewState.Error(it.message) },
        ifRight = { result -> onComplete(result) },
      )
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
