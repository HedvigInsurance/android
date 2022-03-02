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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class AddressAutoCompleteViewModel(
    initialAddress: DanishAddress?,
    getDanishAddressAutoCompletionUseCase: GetDanishAddressAutoCompletionUseCase,
    getFinalDanishAddressSelectionUseCase: GetFinalDanishAddressSelectionUseCase,
) : ViewModel() {

    private val currentInput: MutableStateFlow<DanishAddressInput> = MutableStateFlow(
        DanishAddressInput.fromDanishAddress(initialAddress)
    )
    private val inputResult: Flow<InputResult> = currentInput
        .withHistoryOfLastValue()
        .mapLatest { selectedAddressHistory: ItemWithHistory<DanishAddressInput> ->
            val newInput = selectedAddressHistory.current
            val oldInput = selectedAddressHistory.old
            coroutineScope {
                val finalAddress = async {
                    if (shouldCheckForFinalSelection(newInput.selectedAddress, oldInput)) {
                        val finalAddressResult: FinalAddressResult = getFinalDanishAddressSelectionUseCase.invoke(
                            selectedAddress = newInput.selectedAddress,
                            lastSelection = oldInput.selectedAddress,
                        )
                        if (finalAddressResult is FinalAddressResult.Found) {
                            return@async InputResult(finalAddress = finalAddressResult.address)
                        }
                    }
                    return@async null
                }
                val normalResult = async {
                    getDanishAddressAutoCompletionUseCase
                        .invoke(newInput)
                        .fold(
                            { emptyList() },
                            AddressAutoCompleteResults::resultList
                        )
                        .let(::InputResult)
                }
                return@coroutineScope finalAddress.await() ?: normalResult.await()
            }
        }
        .withHistoryOfLastValue()
        .map { inputResultHistory: ItemWithHistory<InputResult> ->
            val oldResult = inputResultHistory.old
            val newResult = inputResultHistory.current
            if (oldResult != null && newResult.finalAddress != null) {
                // Retain old list of data so that the list doesn't act weird while the screen animates out
                return@map oldResult.copy(finalAddress = newResult.finalAddress)
            }
            newResult
        }

    val viewState: StateFlow<AddressAutoCompleteViewState> = combine(
        currentInput,
        inputResult,
    ) { input, inputResult ->
        AddressAutoCompleteViewState(
            input = input,
            results = inputResult.addressList,
            selectedFinalAddress = inputResult.finalAddress,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AddressAutoCompleteViewState(currentInput.value),
    )

    fun setNewTextInput(inputText: String) {
        currentInput.update { danishAddressInput ->
            danishAddressInput.withNewText(inputText)
        }
    }

    fun selectAddress(address: DanishAddress) {
        currentInput.update { danishAddressInput ->
            danishAddressInput.withSelectedAddress(address)
        }
    }

    @OptIn(ExperimentalContracts::class)
    private fun shouldCheckForFinalSelection(
        selectedAddress: DanishAddress?,
        oldInput: DanishAddressInput?,
    ): Boolean {
        contract {
            returns(true) implies (selectedAddress != null)
            returns(true) implies (oldInput != null)
        }
        // Avoids auto-selecting the same address when opening the screen with an existing address selected
        if (oldInput == null) return false
        return selectedAddress != null
    }
}

data class InputResult(
    val addressList: List<DanishAddress> = emptyList(),
    val finalAddress: DanishAddress? = null,
)

data class AddressAutoCompleteViewState(
    val input: DanishAddressInput,
    val results: List<DanishAddress> = emptyList(),
    val selectedFinalAddress: DanishAddress? = null,
) {
    val showCantFindAddressItem: Boolean
        get() = input.isEmptyInput.not()
}
