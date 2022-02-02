package com.hedvig.app.feature.addressautocompletion.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.addressautocompletion.data.AddressAutoCompleteResults
import com.hedvig.app.feature.addressautocompletion.data.GetDanishAddressAutoCompletionUseCase
import com.hedvig.app.feature.addressautocompletion.model.DanishAddress
import com.hedvig.app.feature.addressautocompletion.model.DanishAddressInput
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class AddressAutoCompleteViewModel(
    initialText: String,
    getDanishAddressAutoCompletionUseCase: GetDanishAddressAutoCompletionUseCase,
) : ViewModel() {

    private val currentInput: MutableStateFlow<DanishAddressInput> = MutableStateFlow(DanishAddressInput(initialText))
    private val queryResults: Flow<List<DanishAddress>> = currentInput
        .debounce(50)
        .mapLatest { input ->
            getDanishAddressAutoCompletionUseCase.invoke(input.queryString)
                .fold(
                    { emptyList() },
                    AddressAutoCompleteResults::resultList
                )
        }
        .onStart { emit(emptyList()) }

    val viewState: StateFlow<AddressAutoCompleteViewState> =
        combine(currentInput, queryResults) { input, results ->
            AddressAutoCompleteViewState(
                input = input,
                results = results,
                error = null, // TODO errors shown when and how?
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
            danishAddressInput.withSelectedAddress(danishAddress)
        }
    }
}

data class AddressAutoCompleteViewState(
    val input: DanishAddressInput,
    val results: List<DanishAddress> = emptyList(),
    val error: String? = null, // temp
)
