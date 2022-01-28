package com.hedvig.app.feature.embark.passages.externalinsurer.retrieveprice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.validateNationalIdentityNumber
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RetrievePriceViewModel(
    private val collectionId: String,
    private val insurerName: String,
    marketManager: MarketManager,
    private val startDataCollectionUseCase: StartDataCollectionUseCase
) : ViewModel() {

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    sealed class Event {
        data class AuthInformation(
            val reference: String
        ) : Event()
    }

    private val _viewState = MutableStateFlow(ViewState(market = marketManager.market))
    val viewState: StateFlow<ViewState> = _viewState

    fun onRetrievePriceInfo() {
        if (viewState.value.inputError != null) {
            return
        }

        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true) }
            val result = startDataCollectionUseCase.startDataCollection(
                personalNumber = viewState.value.input,
                insuranceProvider = collectionId
            )

            when (result) {
                is DataCollectionResult.Error -> {
                    _viewState.update {
                        it.copy(isLoading = false, error = result)
                    }
                }
                is DataCollectionResult.Success -> {
                    _events.trySend(Event.AuthInformation(result.reference))
                    _viewState.update {
                        it.copy(isLoading = false)
                    }
                }
            }
        }
    }

    fun onIdentityInput(input: String) {
        val validationResult = validateNationalIdentityNumber(input)
        _viewState.update {
            it.copy(
                input = input,
                inputError = if (!validationResult.isSuccessful) {
                    ViewState.InputError(
                        errorTextKey = validationResult.errorTextKey ?: 0
                    )
                } else {
                    null
                },
            )
        }
    }

    fun onDismissError() {
        _viewState.update { it.copy(error = null) }
    }

    fun onCollectionStarted(reference: String) {
        _viewState.update { it.copy(collectionStarted = ViewState.CollectionStartedState(reference)) }
    }

    fun onCollectionFailed() {
        _viewState.update { it.copy(collectionFailed = ViewState.CollectionFailedState(insurerName)) }
    }

    fun onRetry() {
        _viewState.update {
            it.copy(
                collectionFailed = null,
                collectionStarted = null
            )
        }
    }

    data class ViewState(
        val input: String = "",
        val inputError: InputError? = null,
        val market: Market?,
        val isLoading: Boolean = false,
        val error: DataCollectionResult.Error? = null,
        val collectionStarted: CollectionStartedState? = null,
        val collectionFailed: CollectionFailedState? = null,
    ) {

        data class CollectionFailedState(
            val insurerName: String
        )

        data class CollectionStartedState(
            val reference: String
        )

        data class InputError(
            val errorTextKey: Int,
        )
    }
}
