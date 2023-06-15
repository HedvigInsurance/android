package com.hedvig.app.feature.loggedin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.chat.data.ChatEventStore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import slimber.log.d

class ReviewDialogViewModel(
  private val chatEventStore: ChatEventStore,
) : ViewModel() {

  val _shouldOpenReviewDialog: Channel<Boolean> = Channel(Channel.CONFLATED)
  val shouldOpenReviewDialog: Flow<Boolean> = _shouldOpenReviewDialog.receiveAsFlow()

  init {
    viewModelScope.launch {
      chatEventStore.observeChatClosedCounter()
        .map { it % 4 == 0 && it != 0 }
        .filter { it == true }
        .collect {
          d { "Will try to show the review dialog" }
          _shouldOpenReviewDialog.send(it)
          chatEventStore.increaseChatClosedCounter()
        }
    }
  }
}
