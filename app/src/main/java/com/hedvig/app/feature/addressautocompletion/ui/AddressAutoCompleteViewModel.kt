package com.hedvig.app.feature.addressautocompletion.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.addressautocompletion.data.AddressAutoCompleteResults
import com.hedvig.app.feature.addressautocompletion.data.GetDanishAddressAutoCompletionUseCase
import com.hedvig.app.feature.addressautocompletion.data.GetFinalDanishAddressSelectionUseCase
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.feature.addressautocompletion.model.DanishAddressInput
import com.hedvig.app.util.coroutines.withHistoryOfLastValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class AddressAutoCompleteViewModel(
    initialAddress: DanishAddress?,
    getDanishAddressAutoCompletionUseCase: GetDanishAddressAutoCompletionUseCase,
    getFinalDanishAddressSelectionUseCase: GetFinalDanishAddressSelectionUseCase,
) : ViewModel() {

    private val currentInput: MutableStateFlow<DanishAddressInput> = MutableStateFlow(
        DanishAddressInput.fromDanishAddress(initialAddress)
    )
    private val queryResults: Flow<List<DanishAddress>> = currentInput
        .debounce(50)
        .mapLatest { input ->
            getDanishAddressAutoCompletionUseCase
                .invoke(input.addressForQuery)
                .fold(
                    { emptyList() },
                    AddressAutoCompleteResults::resultList
                )
        }
        .onStart { emit(emptyList()) }

    val viewState: StateFlow<AddressAutoCompleteViewState> = combine(
        currentInput,
        queryResults,
    ) { input, results ->
        AddressAutoCompleteViewState(
            input = input,
            results = results,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AddressAutoCompleteViewState(currentInput.value),
    )

    private val addressSelectionChannel: Channel<DanishAddress?> = Channel(Channel.UNLIMITED)
    private val addressSelectionHistory = addressSelectionChannel.receiveAsFlow().withHistoryOfLastValue()
    val events: SharedFlow<AddressAutoCompleteEvent> = addressSelectionHistory
        .mapLatest { selectedAddressHistory ->
            val newSelection = selectedAddressHistory.current ?: return@mapLatest null
            val oldSelection = selectedAddressHistory.old
            return@mapLatest getFinalDanishAddressSelectionUseCase.invoke(
                selectedAddress = newSelection,
                lastSelection = oldSelection,
            )
        }
        .filterNotNull()
        .map { address ->
            AddressAutoCompleteEvent.Selection(address)
        }
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            replay = 1
        )

    fun setNewInput(inputText: String) {
        addressSelectionChannel.trySend(null)
        currentInput.update { danishAddressInput ->
            danishAddressInput.withNewText(inputText)
        }
    }

    fun selectAddress(danishAddress: DanishAddress) {
        addressSelectionChannel.trySend(danishAddress)
        currentInput.update { danishAddressInput ->
            danishAddressInput.withSelectedAddress(danishAddress)
        }
    }
}

data class AddressAutoCompleteViewState(
    val input: DanishAddressInput,
    val results: List<DanishAddress> = emptyList(),
)

sealed interface AddressAutoCompleteEvent {
    data class Selection(val selectedAddress: DanishAddress) : AddressAutoCompleteEvent
}
