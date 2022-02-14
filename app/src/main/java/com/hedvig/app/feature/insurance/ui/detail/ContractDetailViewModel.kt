package com.hedvig.app.feature.insurance.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.hanalytics.HAnalytics
import e
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class ContractDetailViewModel : ViewModel() {
    sealed class ViewState {
        data class Success(val state: ContractDetailViewState) : ViewState()
        object Error : ViewState()
        object Loading : ViewState()
    }

    protected val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
    val viewState = _viewState.asStateFlow()

    abstract fun loadContract(id: String)
    abstract suspend fun triggerFreeTextChat()
}

class ContractDetailViewModelImpl(
    contractId: String,
    private val getContractDetailsUseCase: GetContractDetailsUseCase,
    private val chatRepository: ChatRepository,
    hAnalytics: HAnalytics,
) : ContractDetailViewModel() {

    init {
        hAnalytics.screenViewInsuranceDetail(contractId)
        loadContract(contractId)
    }

    override fun loadContract(id: String) {
        viewModelScope.launch {
            val viewState = when (val insurance = getContractDetailsUseCase.invoke(id)) {
                is Either.Left -> ViewState.Error
                is Either.Right -> ViewState.Success(insurance.value)
            }
            _viewState.value = viewState
        }
    }

    override suspend fun triggerFreeTextChat() {
        val response = runCatching { chatRepository.triggerFreeTextChat() }
        if (response.isFailure) {
            response.exceptionOrNull()?.let { e(it) }
        }
    }
}
