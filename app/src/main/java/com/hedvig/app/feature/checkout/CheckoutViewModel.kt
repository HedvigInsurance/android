package com.hedvig.app.feature.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.offer.model.Checkout
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import com.hedvig.app.feature.offer.usecase.CreateAccessTokenUseCase
import com.hedvig.app.feature.offer.usecase.SignQuotesUseCase
import com.hedvig.app.feature.offer.usecase.getquote.GetQuotesUseCase
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.ValidationResult
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.app.util.validateEmail
import com.hedvig.app.util.validateNationalIdentityNumber
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.money.MonetaryAmount
import kotlin.time.Duration.Companion.seconds

class CheckoutViewModel(
    private val quoteIds: List<String>,
    private val quoteCartId: String?,
    private val getQuotesUseCase: GetQuotesUseCase,
    private val signQuotesUseCase: SignQuotesUseCase,
    private val editQuotesUseCase: EditQuotesUseCase,
    private val createAccessTokenUseCase: CreateAccessTokenUseCase,
    private val marketManager: MarketManager,
    private val loginStatusService: LoginStatusService,
    private val hAnalytics: HAnalytics,
    private val featureManager: FeatureManager,
) : ViewModel() {

    init {
        viewModelScope.launch {
            observeQuotes(quoteIds, quoteCartId)
        }
    }

    private suspend fun observeQuotes(quoteIds: List<String>, quoteCartId: String?) {
        getQuotesUseCase.invoke(quoteIds, quoteCartId).collect { result ->
            result.fold(
                ifLeft = { _events.trySend(Event.Error(it.message)) },
                ifRight = { handleOfferModel(it) }
            )
        }
    }

    private suspend fun handleOfferModel(model: OfferModel) {
        if (featureManager.isFeatureEnabled(Feature.QUOTE_CART)) {
            handleCheckoutStatus(model)
        }
        _titleViewState.value = model.mapToViewState()
    }

    private suspend fun handleCheckoutStatus(model: OfferModel) {
        val checkout = model.checkout
        when (checkout?.status) {
            Checkout.CheckoutStatus.PENDING -> Event.Loading
            Checkout.CheckoutStatus.SIGNED -> createAccessToken()
            Checkout.CheckoutStatus.COMPLETED -> _events.trySend(onSignSuccess())
            Checkout.CheckoutStatus.FAILED,
            Checkout.CheckoutStatus.UNKNOWN -> Event.Error(checkout.statusText)
            null -> {}
        }
    }

    private suspend fun createAccessToken() {
        if (quoteCartId == null) {
            _events.trySend(Event.Error("No quote cart id found"))
        } else {
            createAccessTokenUseCase.invoke(quoteCartId)
                .tapLeft { _events.trySend(Event.Error(it.message)) }
        }
    }

    private fun OfferModel.mapToViewState() = TitleViewState.Loaded(
        bundleName = quoteBundle.name,
        netAmount = quoteBundle.cost.netMonthlyCost,
        grossAmount = quoteBundle.cost.grossMonthlyCost,
        market = marketManager.market,
        email = quoteBundle.quotes.firstNotNullOfOrNull(QuoteBundle.Quote::email)
    )

    private val _titleViewState = MutableStateFlow<TitleViewState>(TitleViewState.Loading)
    val titleViewState: StateFlow<TitleViewState> = _titleViewState

    private val _inputViewState = MutableStateFlow(
        InputViewState(
            enableSign = false,
            emailInputState = InputViewState.InputState.NoInput,
            identityInputState = InputViewState.InputState.NoInput
        )
    )
    val inputViewState: StateFlow<InputViewState> = _inputViewState

    private var emailInput: String = ""
    private var identityNumberInput: String = ""

    fun validateInput() {
        _inputViewState.value = _inputViewState.value.copy(
            emailInputState = createInputState(emailInput, ::validateEmail),
            identityInputState = createInputState(identityNumberInput, ::validateNationalIdentityNumber)
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
            enableSign = emailInput.isNotBlank() && identityNumberInput.isNotBlank()
        )
    }

    fun onTrySign(emailInput: String, identityNumberInput: String) {
        if (inputViewState.value.canSign()) {
            _events.trySend(Event.Loading)
            val parameter = createEditAndSignParameter(identityNumberInput, emailInput)
            signQuotes(parameter)
        }
    }

    private fun signQuotes(parameter: EditAndSignParameter) {
        viewModelScope.launch {
            editQuotesUseCase.editQuotes(parameter)
                .map { signQuotesUseCase.signQuotesAndClearCache(quoteIds, quoteCartId) }
                .mapLeft { it.message }
                .fold(
                    ifLeft = {
                        _events.trySend(Event.Error(it))
                    },
                    ifRight = {
                        if (!featureManager.isFeatureEnabled(Feature.QUOTE_CART)) {
                            _events.trySend(it.toEvent())
                        }
                    }
                )
        }
    }

    private fun createEditAndSignParameter(
        identityNumberInput: String,
        emailInput: String
    ) = EditAndSignParameter(
        quoteIds = quoteIds,
        quoteCartId = quoteCartId,
        ssn = identityNumberInput,
        email = emailInput
    )

    private suspend fun SignQuotesUseCase.SignQuoteResult.toEvent(): Event = when (this) {
        is SignQuotesUseCase.SignQuoteResult.Error -> Event.Error(message)
        SignQuotesUseCase.SignQuoteResult.Success -> onSignSuccess()
        else -> Event.Error()
    }

    private suspend fun onSignSuccess(): Event.CheckoutSuccess {
        hAnalytics.quotesSigned(quoteIds)
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
        val identityInputState: InputState
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
