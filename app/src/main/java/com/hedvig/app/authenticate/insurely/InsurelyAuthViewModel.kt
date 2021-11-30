package com.hedvig.app.authenticate.insurely

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InsurelyAuthViewModel(
    private val reference: String,
    private val getDataCollectionUseCase: GetDataCollectionUseCase
) : ViewModel() {

    private val _viewState = MutableStateFlow(ViewState())
    val viewState: StateFlow<ViewState>
        get() = _viewState

    data class ViewState(
        val autoStartToken: String? = null,
        val authStatus: DataCollectionResult.Success.CollectionStatus? = null,
        val error: DataCollectionResult.Error? = null,
    )

    init {
        viewModelScope.launch {
            getDataCollectionUseCase.getCollectionStatus(reference).collect { result ->
                when (result) {
                    is DataCollectionResult.Error -> _viewState.update {
                        it.copy(
                            error = it.error
                        )
                    }
                    is DataCollectionResult.Success.NorwegianBankId -> _viewState.update {
                        it.copy(
                            autoStartToken = result.norwegianBankIdWords,
                            authStatus = result.status,
                            error = null
                        )
                    }
                    is DataCollectionResult.Success.SwedishBankId -> _viewState.update {
                        it.copy(
                            autoStartToken = result.autoStartToken,
                            authStatus = result.status,
                            error = null
                        )
                    }
                }
            }
        }
    }
}
