package com.hedvig.app.feature.embark.passages.previousinsurer.retrieveprice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.R
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.validateNationalIdentityNumber
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RetrievePriceViewModel(
    marketManager: MarketManager
) : ViewModel() {

    private val _viewState = MutableStateFlow(ViewState(marketManager.market))
    val viewState: StateFlow<ViewState> = _viewState

    fun onRetrievePriceInfo() {
        if (viewState.value.isError) {
            return
        }

        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(
                isLoading = true
            )
            delay(1000)
            _viewState.value = viewState.value.copy(
                isLoading = false
            )
        }
    }

    fun onIdentityInput(input: String) {
        val validationResult = validateNationalIdentityNumber(input)
        _viewState.value = _viewState.value.copy(
            input = input,
            isError = !validationResult.isSuccessful,
            errorTextKey = validationResult.errorTextKey
        )
    }

    data class ViewState(
        val input: String = "",
        val isError: Boolean = false,
        val errorTextKey: Int? = null,
        val ssnTitleTextKey: Int = R.string.insurely_se_ssn_title,
        val ssnAssistTextKey: Int = R.string.insurely_se_ssn_assistive_text,
        val ssnInputLabelTextKey: Int = R.string.insurely_se_ssn_input_label,
        val isLoading: Boolean = false,
    ) {

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
