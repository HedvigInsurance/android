package com.hedvig.app.authenticate.insurely

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

class InsurelyAuthViewModel(
    reference: String,
    getDataCollectionUseCase: GetDataCollectionUseCase,
    hAnalytics: HAnalytics,
) : ViewModel() {

    init {
        hAnalytics.screenView(AppScreen.DATA_COLLECTION_AUTHENTICATING)
    }

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    sealed class Event {
        data class Auth(val token: String?) : Event()
    }

    val viewState: StateFlow<ViewState> = getDataCollectionUseCase
        .getCollectionStatus(reference)
        .map { result ->
            when (result) {
                is DataCollectionResult.Error -> ViewState.Error(result)
                is DataCollectionResult.Success.NorwegianBankId -> {
                    _events.trySend(Event.Auth(result.norwegianBankIdWords))
                    ViewState.Success(authStatus = result.status)
                }
                is DataCollectionResult.Success.SwedishBankId -> {
                    _events.trySend(Event.Auth(result.autoStartToken))
                    ViewState.Success(authStatus = result.status)
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds),
            initialValue = ViewState.Loading,
        )

    sealed class ViewState {
        object Loading : ViewState()

        data class Error(
            val error: DataCollectionResult.Error,
        ) : ViewState()

        data class Success(
            val authStatus: DataCollectionResult.Success.CollectionStatus,
        ) : ViewState()
    }
}
