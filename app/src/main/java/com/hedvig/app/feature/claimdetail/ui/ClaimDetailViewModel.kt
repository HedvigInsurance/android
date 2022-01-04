package com.hedvig.app.feature.claimdetail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.chat.usecase.TriggerFreeTextChatUseCase
import com.hedvig.app.feature.claimdetail.data.GetClaimDetailUiStateForClaimIdUseCase
import com.hedvig.app.feature.claimdetail.model.ClaimDetailUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ClaimDetailViewState {
    object Loading : ClaimDetailViewState()
    object Error : ClaimDetailViewState()
    data class Content(
        val uiState: ClaimDetailUiState,
    ) : ClaimDetailViewState()
}

class ClaimDetailViewModel(
    private val claimId: String,
    private val triggerFreeTextChatUseCase: TriggerFreeTextChatUseCase,
    private val getClaimDetailUiStateForClaimIdUseCase: GetClaimDetailUiStateForClaimIdUseCase,
) : ViewModel() {
    sealed class Event {
        object StartChat : Event()
    }

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    val viewState: StateFlow<ClaimDetailViewState> = flow {
        getClaimDetailUiStateForClaimIdUseCase.invoke(claimId)
            .fold(
                ifLeft = {
                    emit(ClaimDetailViewState.Error)
                },
                ifRight = { claimDetailData ->
                    emit(ClaimDetailViewState.Content(claimDetailData))
                },
            )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ClaimDetailViewState.Loading
    )

    fun onChatClick() {
        viewModelScope.launch {
            triggerFreeTextChatUseCase.invoke()
            _events.trySend(Event.StartChat)
        }
    }
}
