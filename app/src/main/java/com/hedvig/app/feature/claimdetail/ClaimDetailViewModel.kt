package com.hedvig.app.feature.claimdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.chat.usecase.TriggerFreeTextChatUseCase
import com.hedvig.app.feature.claimdetail.model.ClaimDetailData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ClaimDetailViewModel(
    private val claimId: String,
    private val triggerFreeTextChatUseCase: TriggerFreeTextChatUseCase,
) : ViewModel() {
    sealed class Event {
        object Chat : Event()
    }

    sealed class ViewState {
        object Loading : ViewState()
        object Error : ViewState()
        data class Content(
            val data: ClaimDetailData,
        ) : ViewState()
    }

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    val viewState: StateFlow<ViewState> = MutableStateFlow(ViewState.Loading).asStateFlow()

    fun onChatClick() {
        viewModelScope.launch {
            triggerFreeTextChatUseCase.invoke()
            _events.trySend(Event.Chat)
        }
    }
}
