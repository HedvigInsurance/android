package com.hedvig.app.feature.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.continuations.either
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.Checkout
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.feature.offer.model.QuoteBundleVariant
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import com.hedvig.app.feature.offer.usecase.CreateAccessTokenUseCase
import com.hedvig.app.feature.offer.usecase.ObserveOfferStateUseCase
import com.hedvig.app.feature.offer.usecase.OfferState
import com.hedvig.app.feature.offer.usecase.StartCheckoutUseCase
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.ValidationResult
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.app.util.validateEmail
import com.hedvig.app.util.validateNationalIdentityNumber
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.money.MonetaryAmount
import kotlin.time.Duration.Companion.seconds

class CheckoutViewModel(
    private val selectedVariantId: String,
    private val quoteCartId: QuoteCartId,
    private val signQuotesUseCase: StartCheckoutUseCase,
    private val editQuotesUseCase: EditCheckoutUseCase,
    private val createAccessTokenUseCase: CreateAccessTokenUseCase,
    private val marketManager: MarketManager,
    private val loginStatusService: LoginStatusService,
    private val offerRepository: OfferRepository,
    private val featureManager: FeatureManager,
    bundleVariantUseCase: ObserveOfferStateUseCase,
) : ViewModel() {

    private val _titleViewState = MutableStateFlow<TitleViewState>(TitleViewState.Loading)
    val titleViewState: StateFlow<TitleViewState> = _titleViewState

    private val _inputViewState = MutableStateFlow(
        InputViewState(
            enableSign = false,
            emailInputState = InputViewState.InputState.NoInput,
            identityInputState = InputViewState.InputState.NoInput,
        ),
    )
    val inputViewState: StateFlow<InputViewState> = _inputViewState

    private var emailInput: String = ""
    private var identityNumberInput: String = ""

    private val offerState = bundleVariantUseCase.observeOfferState(quoteCartId, emptyList()).also {
        bundleVariantUseCase.selectedVariant(selectedVariantId)
    }

    init {
        offerState
            .onEach(::handleOfferResult)
            .launchIn(viewModelScope)
    }

    private suspend fun handleOfferResult(result: Either<ErrorMessage, OfferState>) {
        result.fold(
            ifLeft = { _events.trySend(Event.Error(it.message)) },
            ifRight = {
                handleCheckoutStatus(it.offerModel)
                _titleViewState.value = it.selectedVariant.mapToViewState()
            },
        )
    }

    private suspend fun handleCheckoutStatus(model: OfferModel) {
        val checkout = model.checkout
        when (checkout?.status) {
            Checkout.CheckoutStatus.PENDING -> _events.trySend(Event.Loading)
            Checkout.CheckoutStatus.SIGNED -> createAccessToken()
            Checkout.CheckoutStatus.COMPLETED -> _events.trySend(onSignSuccess())
            Checkout.CheckoutStatus.FAILED,
            Checkout.CheckoutStatus.UNKNOWN,
            -> _events.trySend(Event.Error(checkout.statusText))
            null -> {}
        }
    }

    private suspend fun createAccessToken() {
        createAccessTokenUseCase.invoke(quoteCartId)
            .tapLeft { _events.trySend(Event.Error(it.message)) }
            .tap { offerRepository.queryAndEmitOffer(quoteCartId) }
    }

    private fun QuoteBundleVariant.mapToViewState() = TitleViewState.Loaded(
        bundleName = bundle.name,
        netAmount = bundle.cost.netMonthlyCost,
        grossAmount = bundle.cost.grossMonthlyCost,
        market = marketManager.market,
        email = bundle.quotes.firstNotNullOfOrNull(QuoteBundle.Quote::email),
    )

    fun validateInput() {
        _inputViewState.value = _inputViewState.value.copy(
            emailInputState = createInputState(emailInput, ::validateEmail),
            identityInputState = createInputState(identityNumberInput, ::validateNationalIdentityNumber),
        )
    }

    fun onEmailChanged(input: String, hasError: Boolean) {
        emailInput = input
        setState(hasError)
    }

    fun onIdentityNumberChanged(input: String, hasError: Boolean) {
        identityNumberInput = input
        setState(hasError)
    }

    private fun setState(hasError: Boolean) {
        if (hasError) {
            validateInput()
        }
        setEnabledState()
    }

    private fun createInputState(input: String, validate: (String) -> ValidationResult): InputViewState.InputState {
        return if (input.isEmpty()) {
            InputViewState.InputState.NoInput
        } else {
            val result = validate(input)
            if (result.isSuccessful) {
                InputViewState.InputState.Valid(input)
            } else {
                InputViewState.InputState.Invalid(input, result.errorTextKey)
            }
        }
    }

    private fun setEnabledState() {
        _inputViewState.value = _inputViewState.value.copy(
            enableSign = emailInput.isNotBlank() && identityNumberInput.isNotBlank(),
        )
    }

    fun onTrySign() {
        if (inputViewState.value.canSign()) {
            _events.trySend(Event.Loading)
            signQuotes()
        }
    }

    private fun signQuotes() {
        viewModelScope.launch {
            either<ErrorMessage, StartCheckoutUseCase.Success> {
                val quoteIds = offerState.first().map { it.selectedQuoteIds }.bind()
                val parameter = EditAndSignParameter(
                    quoteIds = quoteIds,
                    quoteCartId = quoteCartId,
                    ssn = identityNumberInput,
                    email = emailInput,
                )
                editQuotesUseCase.editQuotes(parameter).bind()
                signQuotesUseCase.startCheckoutAndClearCache(quoteCartId, quoteIds).bind()
            }.fold(
                ifLeft = { _events.trySend(Event.Error(it.message)) },
                ifRight = { offerRepository.queryAndEmitOffer(quoteCartId) },
            )
        }
    }

    private suspend fun onSignSuccess(): Event.CheckoutSuccess {
        featureManager.invalidateExperiments()
        loginStatusService.isLoggedIn = true
        loginStatusService.isViewingOffer = false
        // Delay sending success in order for the signed quotes to be added on the member
        // Sending success instantly will start HomeFragment, but the member will not have
        // updated contracts.
        delay(5.seconds)
        return Event.CheckoutSuccess
    }

    sealed class TitleViewState {

        data class Loaded(
            val bundleName: String,
            val netAmount: MonetaryAmount,
            val grossAmount: MonetaryAmount,
            val market: Market?,
            val email: String?,
        ) : TitleViewState()

        object Loading : TitleViewState()
    }

    data class InputViewState(
        val enableSign: Boolean,
        val emailInputState: InputState,
        val identityInputState: InputState,
    ) {
        sealed class InputState {
            data class Valid(val input: String) : InputState()
            data class Invalid(val input: String, val stringRes: Int?) : InputState()
            object NoInput : InputState()
        }
    }

    private fun InputViewState.canSign(): Boolean {
        return emailInputState is InputViewState.InputState.Valid &&
            identityInputState is InputViewState.InputState.Valid
    }

    sealed class Event {
        data class Error(val message: String? = null) : Event()

        object CheckoutSuccess : Event()
        object Loading : Event()
    }

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()
}
