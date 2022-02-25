package com.hedvig.app.feature.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.offer.model.OfferModel
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import com.hedvig.app.feature.offer.usecase.SignQuotesUseCase
import com.hedvig.app.feature.offer.usecase.getquote.GetQuotesUseCase
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.ValidationResult
import com.hedvig.app.util.getLeftAndRight
import com.hedvig.app.util.validateEmail
import com.hedvig.app.util.validateNationalIdentityNumber
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.money.MonetaryAmount

class CheckoutViewModel(
    private val quoteIds: List<String>,
    private val quoteCartId: String?,
    private val getQuotesUseCase: GetQuotesUseCase,
    private val signQuotesUseCase: SignQuotesUseCase,
    private val editQuotesUseCase: EditQuotesUseCase,
    private val marketManager: MarketManager,
    private val loginStatusService: LoginStatusService,
    private val hAnalytics: HAnalytics,
) : ViewModel() {

    init {
        viewModelScope.launch {
            getQuotesUseCase.invoke(quoteIds, quoteCartId).onEach { result ->
                when (result) {
                    is GetQuotesUseCase.Result.Success -> _titleViewState.value = result.data.mapToViewState()
                    is GetQuotesUseCase.Result.Error -> _events.trySend(Event.Error(result.message))
                }
            }.launchIn(viewModelScope)
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
            val result = editQuotesUseCase.editAndSignQuotes(parameter)
                .map { signQuotesUseCase.signQuotesAndClearCache(quoteIds, quoteCartId) }
                .mapLeft { Event.Error(it.message) }
                .map { it.toEvent() }

            _events.trySend(result.getLeftAndRight())
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
        SignQuotesUseCase.SignQuoteResult.Success -> {
            hAnalytics.quotesSigned(quoteIds)
            loginStatusService.isLoggedIn = true
            loginStatusService.isViewingOffer = false
            // Delay sending success in order for the signed quotes to be added on the member
            // Sending success instantly will start HomeFragment, but the member will not have
            // updated contracts.
            delay(5000)
            Event.CheckoutSuccess
        }
        else -> Event.Error()
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
