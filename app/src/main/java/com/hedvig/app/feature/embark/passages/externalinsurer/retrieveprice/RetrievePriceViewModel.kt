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
    marketManager: MarketManager,
    private val startDataCollectionUseCase: StartDataCollectionUseCase
) : ViewModel() {

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    sealed class Event {
        data class Error(
            val errorResult: DataCollectionResult.Error
        ) : Event()

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
                    _events.trySend(Event.Error(result))
                    _viewState.update {
                        it.copy(isLoading = false)
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
        _viewState.value = _viewState.value.copy(
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

    fun onCollectionStarted() {
        _viewState.update { it.copy(collectionStarted = true) }
    }

    fun onCollectionFailed() {
        _viewState.update { it.copy(collectionFailed = true) }
    }

    data class ViewState(
        val input: String = "",
        val inputError: InputError? = null,
        val market: Market?,
        val isLoading: Boolean = false,
        val collectionStarted: Boolean = false,
        val collectionFailed: Boolean = false
    ) {

        data class InputError(
            val errorTextKey: Int,
        )
    }
}
