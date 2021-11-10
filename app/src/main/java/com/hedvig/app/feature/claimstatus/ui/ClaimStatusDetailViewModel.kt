package com.hedvig.app.feature.claimstatus.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.claimstatus.model.ClaimStatusDetailData
import com.hedvig.app.feature.claimstatus.usecase.GetClaimStatusDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClaimStatusDetailViewModel(
    private val claimId: String,
    private val getClaimStatusDetailsUseCase: GetClaimStatusDetailsUseCase,
) : ViewModel() {

    private val _viewState: MutableStateFlow<ViewState> = MutableStateFlow(ViewState.Loading)
    val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _viewState.value = ViewState.Loading
            val result = getClaimStatusDetailsUseCase.invoke(claimId)
            _viewState.value = when (result) {
                GetClaimStatusDetailsUseCase.ClaimStatusDetailResult.Error -> ViewState.Error
                is GetClaimStatusDetailsUseCase.ClaimStatusDetailResult.Success -> ViewState.Data(result.data)
            }
        }
    }

    sealed class ViewState {
        object Loading : ViewState()
        object Error : ViewState()
        data class Data(
            val claimStatusDetailData: ClaimStatusDetailData,
        ) : ViewState()
    }
}
