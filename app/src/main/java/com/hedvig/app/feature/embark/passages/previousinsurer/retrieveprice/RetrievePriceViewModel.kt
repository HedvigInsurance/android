package com.hedvig.app.feature.embark.passages.previousinsurer.retrieveprice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.R
import com.hedvig.app.feature.embark.passages.previousinsurer.retrieveprice.StartDataCollectionUseCase.DataCollectionResult.Error
import com.hedvig.app.feature.embark.passages.previousinsurer.retrieveprice.StartDataCollectionUseCase.DataCollectionResult.Success.NorwegianBankId
import com.hedvig.app.feature.embark.passages.previousinsurer.retrieveprice.StartDataCollectionUseCase.DataCollectionResult.Success.SwedishBankId
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.validateNationalIdentityNumber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RetrievePriceViewModel(
    marketManager: MarketManager,
    private val startDataCollectionUseCase: StartDataCollectionUseCase
) : ViewModel() {

    private val _viewState = MutableStateFlow(ViewState(marketManager.market))
    val viewState: StateFlow<ViewState> = _viewState

    fun onRetrievePriceInfo() {
        if (viewState.value.inputError != null) {
            return
        }

        viewModelScope.launch {
            _viewState.value = viewState.value.copy(isLoading = true)
            val result = startDataCollectionUseCase.startDataCollection(
                personalNumber = viewState.value.input,
                insuranceProvider = "se-demo"
            )

            _viewState.value = when (result) {
                is Error -> viewState.value.copy(
                    isLoading = false,
                    error = result.message
                )
                is NorwegianBankId -> viewState.value.copy(
                    isLoading = false,
                    showAuth = true
                )
                is SwedishBankId -> viewState.value.copy(
                    isLoading = false,
                    showAuth = true
                )
            }
        }
    }

    fun onIdentityInput(input: String) {
        val validationResult = validateNationalIdentityNumber(input)
        _viewState.value = _viewState.value.copy(
            input = input,
            inputError = if (!validationResult.isSuccessful) {
                ViewState.InputError(
                    errorTextKey = validationResult.errorTextKey ?: ""
                )
            } else {
                null
            },
        )
    }

    data class ViewState(
        val input: String = "",
        val error: String? = null,
        val inputError: InputError? = null,
        val ssnTitleTextKey: Int = R.string.insurely_se_ssn_title,
        val ssnAssistTextKey: Int = R.string.insurely_se_ssn_assistive_text,
        val ssnInputLabelTextKey: Int = R.string.insurely_se_ssn_input_label,
        val isLoading: Boolean = false,
        val showAuth: Boolean = false,
    ) {

        data class InputError(
            val errorTextKey: Int,
        )

        constructor(market: Market?) : this(
            ssnTitleTextKey = when (market) {
                Market.SE -> R.string.insurely_se_ssn_title
                Market.NO -> R.string.insurely_no_ssn_title
                Market.DK,
                null -> R.string.insurely_se_ssn_title
            },
            ssnAssistTextKey = when (market) {
                Market.SE -> R.string.insurely_se_ssn_assistive_text
                Market.NO -> R.string.insurely_no_ssn_assistive_text
                Market.DK,
                null -> R.string.insurely_se_ssn_assistive_text
            },
            ssnInputLabelTextKey = when (market) {
                Market.SE -> R.string.insurely_se_ssn_input_label
                Market.NO -> R.string.insurely_no_ssn_input_label
                Market.DK,
                null -> R.string.insurely_se_ssn_input_label
            }
        )
    }
}
