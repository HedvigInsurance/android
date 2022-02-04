package com.hedvig.app.feature.offer.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.ui.grossMonthlyCost
import com.hedvig.app.feature.offer.ui.netMonthlyCost
import com.hedvig.app.feature.offer.usecase.GetQuotesUseCase
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.ValidationResult
import com.hedvig.app.util.validateEmail
import com.hedvig.app.util.validateNationalIdentityNumber
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.money.MonetaryAmount

class CheckoutViewModel(
    _quoteIds: List<String>,
    private val getQuotesUseCase: GetQuotesUseCase,
    private val signQuotesUseCase: SignQuotesUseCase,
    private val marketManager: MarketManager,
    private val loginStatusService: LoginStatusService,
    private val hAnalytics: HAnalytics,
) : ViewModel() {

    private lateinit var quoteIds: List<String>

    init {
        viewModelScope.launch {
            when (val idsResult = getQuotesUseCase(_quoteIds)) {
                is GetQuotesUseCase.Result.Success -> {
                    quoteIds = idsResult.ids
                    idsResult
                        .data
                        .onEach(::handleResponse)
                        .catch { _events.trySend(Event.Error(it.message)) }
                        .launchIn(this)
                }
                is GetQuotesUseCase.Result.Error -> {
                    _events.trySend(Event.Error(idsResult.message))
                }
            }
        }
    }

    private fun handleResponse(response: OfferRepository.OfferResult) {
        when (response) {
            is OfferRepository.OfferResult.Error -> {
                _events.trySend(Event.Error(response.message))
            }
            is OfferRepository.OfferResult.Success -> {
                _titleViewState.value = TitleViewState.Loaded(
                    bundleName = response.data.quoteBundle.displayName,
                    netAmount = response.data.netMonthlyCost(),
                    grossAmount = response.data.grossMonthlyCost(),
                    market = marketManager.market,
                    email = response.data.quoteBundle.quotes.firstNotNullOfOrNull(OfferQuery.Quote::email)
                )
            }
        }
    }

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
            signQuotes(
                identityNumberInput = identityNumberInput,
                emailInput = emailInput,
                quoteIds = quoteIds
            )
        }
    }

    private fun signQuotes(identityNumberInput: String, emailInput: String, quoteIds: List<String>) {
        viewModelScope.launch {
            val result = signQuotesUseCase.editAndSignQuotes(
                quoteIds = quoteIds,
                ssn = identityNumberInput,
                email = emailInput
            )
            val event = when (result) {
                is SignQuotesUseCase.SignQuoteResult.Error -> Event.Error(result.message)
                SignQuotesUseCase.SignQuoteResult.Success -> {
                    hAnalytics.quotesSigned(quoteIds.toTypedArray())
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
            _events.trySend(event)
        }
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
