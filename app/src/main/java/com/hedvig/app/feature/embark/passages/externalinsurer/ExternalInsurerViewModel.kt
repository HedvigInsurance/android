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

    private var insuranceProviders: List<InsuranceProvider> = emptyList()

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    sealed class Event {
        data class Continue(
            val providerId: String,
            val providerName: String
        ) : Event()
    }

    private val _viewState = MutableStateFlow(ViewState())
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    data class ViewState(
        val isLoading: Boolean = false,
        val showInsuranceProviders: List<InsuranceProvider>? = null,
        val selectedProvider: InsuranceProvider? = null,
        val error: InsuranceProvidersResult.Error? = null
    ) {
        fun canContinue() = selectedProvider != null
    }

    init {
        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true) }
            when (val result = getInsuranceProvidersUseCase.getInsuranceProviders()) {
                is InsuranceProvidersResult.Success -> _viewState.update {
                    insuranceProviders = result.providers
                    it.copy(isLoading = false)
                }
                is InsuranceProvidersResult.Error -> _viewState.update {
                    it.copy(error = result, isLoading = false)
                }
            }
        }
    }

    fun showInsuranceProviders() {
        _viewState.update {
            it.copy(showInsuranceProviders = insuranceProviders)
        }
    }

    fun selectInsuranceProvider(provider: InsuranceProvider) {
        _viewState.update { it.copy(selectedProvider = provider, showInsuranceProviders = null) }
    }

    fun onContinue() {
        val selectedProvider = _viewState.value.selectedProvider
        if (viewState.value.canContinue() && selectedProvider != null) {
            _events.trySend(
                Event.Continue(
                    providerId = selectedProvider.id,
                    providerName = selectedProvider.name
                )
            )
        }
    }
}
