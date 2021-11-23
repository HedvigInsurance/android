package com.hedvig.app.feature.embark.passages.previousinsurer.retrieveprice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.validateNationalIdentityNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RetrievePriceViewModel(
    marketManager: MarketManager,
    private val startDataCollectionUseCase: StartDataCollectionUseCase
) : ViewModel() {

    private val _viewState = MutableStateFlow(ViewState(market = marketManager.market))
    val viewState: StateFlow<ViewState> = _viewState

    fun onRetrievePriceInfo() {
        if (viewState.value.inputError != null) {
            return
        }

        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true) }
            val result = startDataCollectionUseCase.startDataCollectionAndGetCollectionStatus(
                personalNumber = viewState.value.input,
                insuranceProvider = "ICA"
            )

            when (result) {
                is DataCollectionResult.Error -> _viewState.update { it.copy(error = result, isLoading = false) }
                is DataCollectionResult.Success.SwedishBankId,
                is DataCollectionResult.Success.NorwegianBankId -> _viewState.update {
                    it.copy(
                        showAuth = true,
                        isLoading = false
                    )
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

    fun onDismissError() {
        _viewState.update { it.copy(error = null) }
    }

    data class ViewState(
        val input: String = "",
        val error: DataCollectionResult.Error? = null,
        val inputError: InputError? = null,
        val market: Market?,
        val isLoading: Boolean = false,
        val showAuth: Boolean = false,
    ) {

        data class InputError(
            val errorTextKey: Int,
        )
    }
}
