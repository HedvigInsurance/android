package com.hedvig.app.feature.swedishbankid.sign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.Checkout
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.usecase.CreateAccessTokenUseCase
import com.hedvig.app.util.extensions.mapEitherRight
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.PaymentType
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class SwedishBankIdSignViewModel(
    private val loginStatusService: LoginStatusService,
    private val hAnalytics: HAnalytics,
    private val quoteCartId: QuoteCartId,
    private val offerRepository: OfferRepository,
    private val createAccessTokenUseCase: CreateAccessTokenUseCase,
    private val featureManager: FeatureManager,
) : ViewModel() {
    sealed class ViewState {
        object StartClient : ViewState()
        object InProgress : ViewState()
        object Cancelled : ViewState()
        object Error : ViewState()
        object Success : ViewState()
    }

    private val _viewState = MutableStateFlow<ViewState>(ViewState.StartClient)
    val viewState = _viewState.asStateFlow()

    private var signStatusJob: Job? = null

    sealed class Event {
        data class StartDirectDebit(val payinType: PaymentType) : Event()
        object StartBankID : Event()
    }

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    private var hasCompletedSign = false

    init {
        _events.trySend(Event.StartBankID)
        observeOfferSignState(quoteCartId)
    }

    fun manuallyRecheckSignStatus() {
        signStatusJob = viewModelScope.launch {
            while (!hasCompletedSign) {
                offerRepository.queryAndEmitOffer(quoteCartId)
                delay(500)
            }
        }
    }

    fun cancelSignStatusPolling() {
        signStatusJob?.cancel()
    }

    private fun observeOfferSignState(quoteCartId: QuoteCartId) {
        offerRepository.offerFlow
            .mapEitherRight { offer -> offer.checkout }
            .onEach { result ->
                val state = when (result) {
                    is Either.Left -> ViewState.Error
                    is Either.Right -> toViewState(result.value)
                }
                _viewState.value = state
                if (state is ViewState.Success && !hasCompletedSign) {
                    when (createAccessTokenUseCase.invoke(quoteCartId)) {
                        is Either.Left -> _viewState.value = ViewState.Error
                        is Either.Right -> completeSign()
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun completeSign() {
        hasCompletedSign = true
        hAnalytics.quotesSigned(listOf(quoteCartId.id))
        loginStatusService.isViewingOffer = false
        loginStatusService.isLoggedIn = true
        viewModelScope.launch {
            featureManager.invalidateExperiments()
            delay(1.seconds)
            _events.trySend(Event.StartDirectDebit(featureManager.getPaymentType()))
        }
    }

    private fun toViewState(checkout: Checkout?): ViewState {
        return when (checkout?.status) {
            Checkout.CheckoutStatus.COMPLETED -> ViewState.Success
            Checkout.CheckoutStatus.SIGNED -> ViewState.Success
            Checkout.CheckoutStatus.PENDING -> ViewState.InProgress
            Checkout.CheckoutStatus.FAILED -> ViewState.Error
            Checkout.CheckoutStatus.UNKNOWN -> ViewState.Error
            null -> ViewState.InProgress
        }
    }
}
