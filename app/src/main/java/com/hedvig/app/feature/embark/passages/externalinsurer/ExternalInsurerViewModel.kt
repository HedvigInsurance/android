package com.hedvig.app.feature.embark.passages.externalinsurer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExternalInsurerViewModel(
    private val getInsuranceProvidersUseCase: GetInsuranceProvidersUseCase
) : ViewModel() {

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    sealed class Event {
        data class Error(
            val errorResult: InsuranceProvidersResult.Error
        ) : Event()
    }

    private val _viewState = MutableStateFlow(ViewState())
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    data class ViewState(
        val isLoading: Boolean = false,
        val insuranceProviders: List<InsuranceProvider>? = null,
        val selectedProvider: InsuranceProvider? = null,
    ) {
        fun canContinue() = selectedProvider != null
    }

    init {
        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true) }
            when (val result = getInsuranceProvidersUseCase.getInsuranceProviders()) {
                is InsuranceProvidersResult.Success -> _viewState.update {
                    it.copy(isLoading = false, insuranceProviders = result.providers)
                }
                is InsuranceProvidersResult.Error -> {
                    _viewState.update { it.copy(isLoading = false) }
                    _events.trySend(Event.Error(result))
                }
            }
        }
    }

    fun selectInsuranceProvider(provider: InsuranceProvider) {
        _viewState.update { it.copy(selectedProvider = provider) }
    }
}
