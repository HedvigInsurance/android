package com.hedvig.app.feature.embark.passages.externalinsurer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExternalInsurerViewModel(
    private val getInsuranceProvidersUseCase: GetInsuranceProvidersUseCase
) : ViewModel() {

    private var insuranceProviders: List<InsuranceProvider> = emptyList()

    private val _viewState = MutableStateFlow(ViewState())
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    data class ViewState(
        val isLoading: Boolean = false,
        val showInsuranceProviders: List<InsuranceProvider>? = null,
        val selectedProvider: InsuranceProvider? = null,
        val error: InsuranceProvidersResult.Error? = null,
        val continueEvent: ContinueEvent? = null,
    ) {
        data class ContinueEvent(val providerId: String, val providerName: String)

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
            val event = ViewState.ContinueEvent(
                providerId = selectedProvider.id,
                providerName = selectedProvider.name
            )
            _viewState.update {
                it.copy(continueEvent = event)
            }
        }
    }
}
