package com.hedvig.app.feature.embark.passages.previousinsurer.retrieveprice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.util.validateNationalIdentityNumber
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RetrievePriceViewModel : ViewModel() {

    private val _viewState = MutableStateFlow(ViewState())
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
        val isLoading: Boolean = false
    )
}
