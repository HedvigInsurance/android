package com.hedvig.app.feature.embark.passages.previousinsurer.retrieveprice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RetrievePriceViewModel : ViewModel() {

    private val _viewState = MutableStateFlow<ViewState>(ViewState.RetrievePrice)
    val viewState: StateFlow<ViewState> = _viewState

    fun onRetrievePriceInfo() {
        viewModelScope.launch {
            _viewState.value = ViewState.Loading
            delay(1000)
            _viewState.value = ViewState.RetrievePrice
        }
    }

    fun onIdentityInput(input: String) {
    }

    sealed class ViewState {
        object Loading : ViewState()
        object RetrievePrice : ViewState()
    }
}
