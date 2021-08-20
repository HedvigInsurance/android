package com.hedvig.app.feature.swedishbankid.sign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.type.BankIdStatus
import com.hedvig.android.owldroid.type.SignState
import com.hedvig.app.feature.swedishbankid.sign.usecase.ManuallyRecheckSwedishBankIdSignStatusUseCase
import com.hedvig.app.feature.swedishbankid.sign.usecase.SubscribeToSwedishBankIdSignStatusUseCase
import e
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SwedishBankIdSignViewModel(
    autoStartToken: String,
    subscribeToSwedishBankIdSignStatusUseCase: SubscribeToSwedishBankIdSignStatusUseCase,
    private val manuallyRecheckSwedishBankIdSignStatusUseCase: ManuallyRecheckSwedishBankIdSignStatusUseCase
) : ViewModel() {
    sealed class ViewState {
        object StartClient : ViewState()
        object InProgress : ViewState()
        object Cancelled : ViewState()
        object Error : ViewState()
        object Success : ViewState()
    }

    private val _viewState = MutableStateFlow<ViewState>(
        ViewState.StartClient
    )
    val viewState = _viewState.asStateFlow()

    sealed class Event {
        data class StartBankID(val autoStartToken: String) : Event()
        object StartDirectDebit : Event()
    }

    private val _events = MutableSharedFlow<Event>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<Event> = _events

    init {
        _events.tryEmit(Event.StartBankID(autoStartToken))

        subscribeToSwedishBankIdSignStatusUseCase()
            .onEach { response ->
                toViewStateOrNull(response.data?.signStatus?.status?.fragments?.signStatusFragment)?.let {
                    _viewState.value = it
                    if (it is ViewState.Success) {
                        viewModelScope.launch {
                            delay(Duration.seconds(1))
                            _events.tryEmit(Event.StartDirectDebit)
                        }
                    }
                }
            }
            .catch { ex ->
                e(ex)
                _viewState.value = ViewState.Error
            }
            .launchIn(viewModelScope)
    }

    fun manuallyRecheckSignStatus() {
        viewModelScope.launch {
            manuallyRecheckSwedishBankIdSignStatusUseCase()
                ?.let(::toViewStateOrNull)
                ?.let { _viewState.value = it }
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
                    SignState.COMPLETED -> {
                        ViewState.Success
                    }
                    else -> ViewState.Error
                }
            }
            else -> null
        }
    }
}
