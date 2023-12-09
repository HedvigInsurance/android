package com.hedvig.app.feature.loggedin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.feature.chat.closedevent.ChatClosedEventStore
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ReviewDialogViewModel(
  private val chatClosedEventStore: ChatClosedEventStore,
) : ViewModel() {
  val _shouldOpenReviewDialog: Channel<Boolean> = Channel(Channel.CONFLATED)
  val shouldOpenReviewDialog: Flow<Boolean> = _shouldOpenReviewDialog.receiveAsFlow()

  init {
    viewModelScope.launch {
      chatClosedEventStore.observeChatClosedCounter()
        .map { it % 4 == 0 && it != 0 }
        .filter { it == true }
        .collect {
          logcat { "Will try to show the review dialog" }
          _shouldOpenReviewDialog.send(it)
          chatClosedEventStore.increaseChatClosedCounter()
        }
    }
  }
}
