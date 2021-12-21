package com.hedvig.app.feature.claimdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.chat.usecase.TriggerFreeTextChatUseCase
import com.hedvig.app.feature.claimdetail.data.GetClaimDetailDataForClaimIdUseCase
import com.hedvig.app.feature.claimdetail.model.ClaimDetailsData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClaimDetailViewModel(
    private val claimId: String,
    private val triggerFreeTextChatUseCase: TriggerFreeTextChatUseCase,
    private val getClaimDetailDataForClaimIdUseCase: GetClaimDetailDataForClaimIdUseCase,
) : ViewModel() {
    sealed class Event {
        object Chat : Event()
    }

    sealed class ViewState {
        object Loading : ViewState()
        object Error : ViewState()
        data class Content(
            val data: ClaimDetailsData,
        ) : ViewState()
    }

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    val viewState: StateFlow<ViewState> = flow {
        getClaimDetailDataForClaimIdUseCase.invoke(claimId)
            .fold(
                { emit(ViewState.Error) },
                { claimDetailData ->
                    emit(ViewState.Content(claimDetailData))
                },
            )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ViewState.Loading
    )

    fun onChatClick() {
        viewModelScope.launch {
            triggerFreeTextChatUseCase.invoke()
            _events.trySend(Event.Chat)
        }
    }
}
