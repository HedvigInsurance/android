package com.hedvig.app.feature.claimstatus.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.ClaimStatusDetailsQuery
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
            val data = getClaimStatusDetailsUseCase.invoke(claimId)
            _viewState.value = if (data == null) {
                ViewState.Error
            } else {
                ViewState.Data(data)
            }
        }
    }

    companion object {
        sealed class ViewState {
            object Loading : ViewState()
            object Error : ViewState()
            data class Data(
                val claimStatusDetail: ClaimStatusDetailsQuery.ClaimStatusDetail,
            ) : ViewState()
        }
    }
}
