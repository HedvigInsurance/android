package com.hedvig.app.feature.loggedin.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.chat.data.ChatEventStore
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import slimber.log.d

class ReviewDialogViewModel(
  private val chatEventStore: ChatEventStore,
) : ViewModel() {

  private val _shouldOpenReviewDialog = MutableSharedFlow<Boolean>(
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
  ).also {
    it.onEach {
      d { "Will try to show the review dialog" }
    }
  }
  val shouldOpenReviewDialog: SharedFlow<Boolean> = _shouldOpenReviewDialog.asSharedFlow()

  init {
    viewModelScope.launch {
      chatEventStore.observeChatClosedCounter()
        .map { it % 3 == 0 && it != 0 }
        .collect(_shouldOpenReviewDialog::tryEmit)
    }
  }
}
