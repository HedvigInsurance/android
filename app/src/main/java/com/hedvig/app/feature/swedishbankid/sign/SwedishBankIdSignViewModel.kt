package com.hedvig.app.feature.swedishbankid.sign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.type.BankIdStatus
import com.hedvig.android.owldroid.type.SignState
import com.hedvig.app.authenticate.LoginStatusService
import com.hedvig.app.feature.offer.OfferRepository
import com.hedvig.app.feature.offer.model.Checkout
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.feature.offer.usecase.CreateAccessTokenUseCase
import com.hedvig.app.feature.swedishbankid.sign.usecase.ManuallyRecheckSwedishBankIdSignStatusUseCase
import com.hedvig.app.feature.swedishbankid.sign.usecase.SubscribeToSwedishBankIdSignStatusUseCase
import com.hedvig.app.util.extensions.mapEitherRight
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.app.util.featureflags.flags.Feature
import com.hedvig.hanalytics.HAnalytics
import e
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class SwedishBankIdSignViewModel(
    autoStartToken: String?,
    subscribeToSwedishBankIdSignStatusUseCase: SubscribeToSwedishBankIdSignStatusUseCase,
    private val manuallyRecheckSwedishBankIdSignStatusUseCase: ManuallyRecheckSwedishBankIdSignStatusUseCase,
    private val loginStatusService: LoginStatusService,
    private val hAnalytics: HAnalytics,
    private val quoteIds: List<String>,
    private val quoteCartId: QuoteCartId?,
    private val offerRepository: OfferRepository,
    private val featureManager: FeatureManager,
    private val createAccessTokenUseCase: CreateAccessTokenUseCase,
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

    sealed class Event {
        data class StartBankID(val autoStartToken: String?) : Event()
        object StartDirectDebit : Event()
    }

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    private var hasCompletedSign = false

    init {
        _events.trySend(Event.StartBankID(autoStartToken))

        if (quoteCartId != null) {
            observeOfferSignState(quoteCartId)
        } else {
            subscribeToSwedishBankIdSignStatusUseCase()
                .onEach { response ->
                    handleNewSignStatus(response.data?.signStatus?.status?.fragments?.signStatusFragment)
                }
                .catch { ex ->
                    e(ex)
                    _viewState.value = ViewState.Error
                }
                .launchIn(viewModelScope)
        }
    }

    fun manuallyRecheckSignStatus() {
        viewModelScope.launch {
            if (featureManager.isFeatureEnabled(Feature.QUOTE_CART)) {
                while (true) {
                    offerRepository.queryAndEmitOffer(quoteCartId, emptyList())
                    delay(500)
                }
            } else {
                manuallyRecheckSwedishBankIdSignStatusUseCase()?.let(::handleNewSignStatus)
            }
        }
    }

    private fun handleNewSignStatus(signStatusFragment: SignStatusFragment?) {
        val newViewState = toViewStateOrNull(signStatusFragment) ?: return
        _viewState.value = newViewState
        if (newViewState is ViewState.Success && !hasCompletedSign) {
            completeSign()
        }
    }

    private fun observeOfferSignState(quoteCartId: QuoteCartId) {
        offerRepository.offerFlow(emptyList())
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
        hAnalytics.quotesSigned(quoteIds)
        loginStatusService.isViewingOffer = false
        loginStatusService.isLoggedIn = true
        viewModelScope.launch {
            delay(1.seconds)
            _events.trySend(Event.StartDirectDebit)
        }
    }

    private fun toViewStateOrNull(status: SignStatusFragment?): ViewState? {
        if (status == null) {
            return ViewState.Error
        }
        return when (status.collectStatus?.status) {
            BankIdStatus.PENDING -> {
                when (status.collectStatus?.code) {
                    "noClient" -> ViewState.StartClient
                    "unknown", "userSign" -> ViewState.InProgress
                    else -> null
                }
            }
            BankIdStatus.FAILED -> {
                when (status.collectStatus?.code) {
                    "userCancel", "cancelled" -> ViewState.Cancelled
                    else -> ViewState.Error
                }
            }
            BankIdStatus.COMPLETE -> {
                when (status.signState) {
                    SignState.INITIATED, SignState.IN_PROGRESS -> null
                    SignState.COMPLETED -> ViewState.Success
                    else -> ViewState.Error
                }
            }
            else -> null
        }
    }

    private fun toViewState(checkout: Checkout?): ViewState {
        return when (checkout?.status) {
            Checkout.CheckoutStatus.COMPLETED -> ViewState.Success
            Checkout.CheckoutStatus.SIGNED -> ViewState.Success
            Checkout.CheckoutStatus.PENDING -> ViewState.InProgress
            Checkout.CheckoutStatus.FAILED -> ViewState.Error
            Checkout.CheckoutStatus.UNKNOWN -> ViewState.Error
            null -> ViewState.Error
        }
    }
}
