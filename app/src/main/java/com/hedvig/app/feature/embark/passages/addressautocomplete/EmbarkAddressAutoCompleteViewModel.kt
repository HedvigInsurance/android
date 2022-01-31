package com.hedvig.app.feature.embark.passages.addressautocomplete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class EmbarkAddressAutoCompleteViewModel(
    initialMessages: List<String>,
    initialAddress: String,
) : ViewModel() {

    data class ViewState(
        val messages: List<String>,
        val address: String,
    )

    private val messages: MutableStateFlow<List<String>> = MutableStateFlow(initialMessages)
    private val address: MutableStateFlow<String> = MutableStateFlow(initialAddress)

    val viewState: StateFlow<ViewState> = combine(messages, address) { messages, address ->
        ViewState(messages, address)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ViewState(initialMessages, initialAddress),
    )

    fun updateAddressSelected(newAddress: String) {
        address.update {
            newAddress
        }
    }
}
