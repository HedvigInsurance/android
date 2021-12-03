package com.hedvig.app.feature.claimdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.chat.usecase.TriggerFreeTextChatUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ClaimDetailViewModel(
    private val triggerFreeTextChatUseCase: TriggerFreeTextChatUseCase,
) : ViewModel() {
    sealed class Event {
        object Chat : Event()
    }

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    fun onChatClick() {
        viewModelScope.launch {
            triggerFreeTextChatUseCase.invoke()
            _events.trySend(Event.Chat)
        }
    }
}
