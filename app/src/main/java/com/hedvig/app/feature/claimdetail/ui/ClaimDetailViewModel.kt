package com.hedvig.app.feature.claimdetail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.claimdetail.data.GetClaimDetailUiStateFlowUseCase
import com.hedvig.app.feature.claimdetail.model.ClaimDetailUiState
import com.hedvig.app.util.coroutines.RetryChannel
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
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
    private val chatRepository: ChatRepository,
    private val getClaimDetailUiStateFlowUseCase: GetClaimDetailUiStateFlowUseCase,
    private val hAnalytics: HAnalytics,
) : ViewModel() {
    sealed class Event {
        object StartChat : Event()
        object Error : Event()
    }

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    private val retryChannel = RetryChannel()
    val viewState: StateFlow<ClaimDetailViewState> = retryChannel.transformLatest {
        emit(ClaimDetailViewState.Loading)
        getClaimDetailUiStateFlowUseCase.invoke(claimId)
            .collect { result ->
                result.fold(
                    ifLeft = { emit(ClaimDetailViewState.Error) },
                    ifRight = { claimDetailUiState ->
                        emit(ClaimDetailViewState.Content(claimDetailUiState))
                    },
                )
            }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ClaimDetailViewState.Loading
    )

    fun retry() {
        retryChannel.retry()
    }

    fun onChatClick() {
        viewModelScope.launch {
            val event = when (chatRepository.triggerFreeTextChat()) {
                is Either.Left -> Event.Error
                is Either.Right -> Event.StartChat
            }
            _events.trySend(event)
        }
        val uiState = (viewState.value as? ClaimDetailViewState.Content)
            ?.uiState ?: return

        hAnalytics.claimDetailClickOpenChat(claimId, uiState.claimStatus.rawValue)
    }
}
