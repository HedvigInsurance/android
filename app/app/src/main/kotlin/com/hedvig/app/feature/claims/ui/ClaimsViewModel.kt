package com.hedvig.app.feature.claims.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.app.feature.chat.data.ChatRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ClaimsViewModel(
  private val chatRepository: ChatRepository,
) : ViewModel() {

  sealed class Event {
    object StartChat : Event()
    object Error : Event()
  }

  private val _events = Channel<Event>(Channel.UNLIMITED)
  val events = _events.receiveAsFlow()

  suspend fun triggerFreeTextChat() {
    viewModelScope.launch {
      val event = when (chatRepository.triggerFreeTextChat()) {
        is Either.Left -> Event.Error
        is Either.Right -> Event.StartChat
      }
      _events.trySend(event)
    }
  }
}
