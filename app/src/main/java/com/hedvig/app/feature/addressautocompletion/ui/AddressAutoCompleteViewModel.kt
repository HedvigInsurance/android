package com.hedvig.app.feature.addressautocompletion.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.addressautocompletion.data.AddressAutoCompleteResults
import com.hedvig.app.feature.addressautocompletion.data.GetDanishAddressAutoCompletionUseCase
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.feature.addressautocompletion.model.DanishAddressInput
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class AddressAutoCompleteViewModel(
    initialAddress: DanishAddress?,
    getDanishAddressAutoCompletionUseCase: GetDanishAddressAutoCompletionUseCase,
) : ViewModel() {

    private val currentInput: MutableStateFlow<DanishAddressInput> = MutableStateFlow(
        DanishAddressInput.fromDanishAddress(initialAddress)
    )
    private val queryResults: Flow<List<DanishAddress>> = currentInput
        .debounce(50)
        .mapLatest { input ->
            getDanishAddressAutoCompletionUseCase.invoke(input)
                .fold(
                    { emptyList() },
                    AddressAutoCompleteResults::resultList
                )
        }
        .onStart { emit(emptyList()) }

    private val checkSelectionChannel: Channel<Pair<DanishAddress, List<DanishAddress>>> = Channel(Channel.UNLIMITED)
    val resultEvent = checkSelectionChannel.receiveAsFlow()
        .mapLatest { (selectedAddress, addressList) ->
            val address = getFinalResultOrNull(selectedAddress, addressList) ?: return@mapLatest null
            AddressAutoCompleteEvent.Selection(address)
        }
        .filterNotNull()

    val viewState: StateFlow<AddressAutoCompleteViewState> =
        combine(currentInput, queryResults) { input, results ->
            AddressAutoCompleteViewState(
                input = input,
                results = results,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AddressAutoCompleteViewState(currentInput.value),
        )

    fun setNewInput(inputText: String) {
        currentInput.update { danishAddressInput ->
            danishAddressInput.withNewInput(inputText)
        }
    }

    fun selectAddress(danishAddress: DanishAddress) {
        currentInput.update { danishAddressInput ->
            checkSelectionChannel.trySend(danishAddress to viewState.value.results)
            danishAddressInput.withSelectedAddress(danishAddress)
        }
    }

    private fun getFinalResultOrNull(
        address: DanishAddress?,
        results: List<DanishAddress>,
    ): DanishAddress? {
        val selectedAddress = address ?: return null
        if (results.size != 1) return null
        if (selectedAddress.isValidFinalSelection.not()) return null
        if (selectedAddress.address != results.first().address) return null
        return selectedAddress
    }
}

data class AddressAutoCompleteViewState(
    val input: DanishAddressInput,
    val results: List<DanishAddress> = emptyList(),
)

sealed interface AddressAutoCompleteEvent {
    data class Selection(val selectedAddress: DanishAddress) : AddressAutoCompleteEvent
}
