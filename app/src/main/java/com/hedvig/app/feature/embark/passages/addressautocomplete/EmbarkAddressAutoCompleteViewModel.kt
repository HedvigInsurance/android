package com.hedvig.app.feature.embark.passages.addressautocomplete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class EmbarkAddressAutoCompleteViewModel(
    initialAddress: String,
) : ViewModel() {

    data class ViewState(
        val address: String,
    )

    private val address: MutableStateFlow<String> = MutableStateFlow(initialAddress)
    val viewState: StateFlow<ViewState> = address.map { address ->
        ViewState(address)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ViewState(initialAddress),
    )

    fun updateAddressSelected(newAddress: String) {
        address.update {
            newAddress
        }
    }
}
