package com.hedvig.app.authenticate.insurely

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class InsurelyAuthViewModel(
    reference: String,
    getDataCollectionUseCase: GetDataCollectionUseCase
) : ViewModel() {

    val viewState: StateFlow<ViewState> = getDataCollectionUseCase
        .getCollectionStatus(reference)
        .map { result ->
            when (result) {
                is DataCollectionResult.Error -> ViewState.Error(result)
                is DataCollectionResult.Success.NorwegianBankId -> ViewState.Success(
                    autoStartToken = result.norwegianBankIdWords,
                    authStatus = result.status,
                )
                is DataCollectionResult.Success.SwedishBankId -> ViewState.Success(
                    autoStartToken = result.autoStartToken,
                    authStatus = result.status,
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ViewState.Loading
        )

    sealed class ViewState {
        object Loading : ViewState()

        data class Error(
            val error: DataCollectionResult.Error
        ) : ViewState()

        data class Success(
            val autoStartToken: String?,
            val authStatus: DataCollectionResult.Success.CollectionStatus,
        ) : ViewState()
    }
}
