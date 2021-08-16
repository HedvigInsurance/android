package com.hedvig.app.feature.embark.passages.previousinsurer.askforprice

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AskForPriceViewModel(
    parameter: AskForPriceInfoParameter
) : ViewModel() {
    private val _selectedInsurance = MutableStateFlow(parameter.selectedInsuranceProvider)
    val selectedInsurance: StateFlow<String> = _selectedInsurance
}
