package com.hedvig.app.feature.addressautocompletion.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.addressautocompletion.data.AddressAutoCompleteResults
import com.hedvig.app.feature.addressautocompletion.data.FinalAddressResult
import com.hedvig.app.feature.addressautocompletion.data.GetDanishAddressAutoCompletionUseCase
import com.hedvig.app.feature.addressautocompletion.data.GetFinalDanishAddressSelectionUseCase
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.feature.addressautocompletion.model.DanishAddressInput
import com.hedvig.app.util.coroutines.ItemWithHistory
import com.hedvig.app.util.coroutines.withHistoryOfLastValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
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
        .mapLatest { input ->
            if (input.selectedAddress != null) {
                getDanishAddressAutoCompletionUseCase.invoke(input.selectedAddress)
            } else {
                getDanishAddressAutoCompletionUseCase.invoke(input.rawText)
            }
                .fold(
                    { emptyList() },
                    AddressAutoCompleteResults::resultList
                )
        }
        .onStart { emit(emptyList()) }

    private val addressSelectionChannel: Channel<DanishAddress?> = Channel(Channel.UNLIMITED)
    private val addressSelectionHistory: Flow<ItemWithHistory<DanishAddress?>> =
        addressSelectionChannel.receiveAsFlow().withHistoryOfLastValue()
    private val selectedFinalAddress: Flow<DanishAddress?> = addressSelectionHistory
        .mapLatest { selectedAddressHistory ->
            val newSelection = selectedAddressHistory.current ?: return@mapLatest null
            val oldSelection = selectedAddressHistory.old
            getFinalDanishAddressSelectionUseCase.invoke(
                selectedAddress = newSelection,
                lastSelection = oldSelection,
            )
        }
        .filterNotNull()
        .mapNotNull { finalAddressResult ->
            when (finalAddressResult) {
                is FinalAddressResult.Found -> finalAddressResult.address
                FinalAddressResult.NetworkError -> null
                FinalAddressResult.NotFinalAddress -> null
            }
        }
        .onStart<DanishAddress?> { emit(null) }

    val viewState: StateFlow<AddressAutoCompleteViewState> = combine(
        currentInput,
        queryResults,
        selectedFinalAddress,
    ) { input, results, selectedFinalAddress ->
        AddressAutoCompleteViewState(
            input = input,
            results = results,
            selectedFinalAddress = selectedFinalAddress,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AddressAutoCompleteViewState(currentInput.value),
    )

    fun setNewTextInput(inputText: String) {
        addressSelectionChannel.trySend(null)
        currentInput.update { danishAddressInput ->
            danishAddressInput.withNewText(inputText)
        }
    }

    fun selectAddress(address: DanishAddress) {
        addressSelectionChannel.trySend(address)
        currentInput.update { danishAddressInput ->
            danishAddressInput.withSelectedAddress(address)
        }
    }
}

data class AddressAutoCompleteViewState(
    val input: DanishAddressInput,
    val results: List<DanishAddress> = emptyList(),
    val selectedFinalAddress: DanishAddress? = null,
) {
    val showCantFindAddressItem: Boolean
        get() = input.isEmptyInput.not()
}
