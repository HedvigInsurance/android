package com.hedvig.android.core.appreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class ReviewDialogViewModel(
  private val selfServiceCompletedEventStore: SelfServiceCompletedEventStore,
) : ViewModel() {
  private val _shouldOpenReviewDialog: Channel<Boolean> = Channel(Channel.CONFLATED)
  val shouldOpenReviewDialog: Flow<Boolean> = _shouldOpenReviewDialog.receiveAsFlow()

  init {
    viewModelScope.launch {
      selfServiceCompletedEventStore.observeNumberOfCompletedSelfServices()
        .map { it > 0 }
        .filter { it }
        .collect {
          logcat { "Will try to show the review dialog" }
          _shouldOpenReviewDialog.send(it)
        }
    }
  }
}
