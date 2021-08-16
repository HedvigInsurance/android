package com.hedvig.app.feature.loggedin.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.feature.chat.data.ChatEventStore
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

abstract class LoggedInViewModel : ViewModel() {
    protected val _data = MutableLiveData<LoggedInQuery.Data>()
    val data: LiveData<LoggedInQuery.Data> = _data

    private val _scroll = MutableLiveData<Int>()
    val scroll: LiveData<Int> = _scroll

    protected val _shouldOpenReviewDialog = MutableSharedFlow<Boolean>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val shouldOpenReviewDialog: SharedFlow<Boolean> = _shouldOpenReviewDialog.asSharedFlow()

    fun onScroll(scroll: Int) {
        _scroll.postValue(scroll)
    }

    abstract fun onReviewByChatComplete()
}

class LoggedInViewModelImpl(
    private val loggedInRepository: LoggedInRepository,
    private val chatEventStore: ChatEventStore
) : LoggedInViewModel() {

    init {
        viewModelScope.launch {
            chatEventStore.observeChatClosedCounter()
                .map { it == 3 }
                .collect(_shouldOpenReviewDialog::tryEmit)
        }

        viewModelScope.launch {
            val response = runCatching {
                loggedInRepository.loggedInData()
            }

            response.getOrNull()?.data?.let { _data.postValue(it) }
        }
    }

    override fun onReviewByChatComplete() {
        viewModelScope.launch {
            chatEventStore.resetChatClosedCounter()
        }
    }
}
