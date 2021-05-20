package com.hedvig.app.feature.loggedin.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.LoggedInQuery
<<<<<<< HEAD
import com.hedvig.app.feature.chat.data.ChatEventStore
import com.hedvig.app.feature.loggedin.service.TabNotificationService
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
=======
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
>>>>>>> 69cfc2025... Start work on injecting mock modules
import kotlinx.coroutines.launch

abstract class AbstractLoggedInViewModel : ViewModel() {
    protected val _data = MutableLiveData<LoggedInQuery.Data>()
    val data: LiveData<LoggedInQuery.Data> = _data

    private val _scroll = MutableLiveData<Int>()
    val scroll: LiveData<Int> = _scroll

    protected val _shouldOpenReviewDialog = MutableSharedFlow<Boolean>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val shouldOpenReviewDialog: SharedFlow<Boolean> = _shouldOpenReviewDialog.asSharedFlow()

    protected val _tabNotifications = MutableStateFlow<Set<LoggedInTabs>>(emptySet())
    val tabNotifications = _tabNotifications.asStateFlow()

    fun onScroll(scroll: Int) {
        _scroll.postValue(scroll)
    }

    abstract fun onReviewByChatComplete()

    abstract fun onTabVisited(tab: LoggedInTabs)
}

<<<<<<< HEAD
class LoggedInViewModelImpl(
    private val loggedInRepository: LoggedInRepository,
    private val chatEventStore: ChatEventStore,
    private val tabNotificationService: TabNotificationService,
) : LoggedInViewModel() {
=======
@HiltViewModel
class LoggedInViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val loggedInRepository: LoggedInRepository
) : AbstractLoggedInViewModel() {
>>>>>>> 69cfc2025... Start work on injecting mock modules

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
        viewModelScope.launch {
            tabNotificationService
                .load()
                .collect {
                    _tabNotifications.value = it
                }
        }
    }

    override fun onReviewByChatComplete() {
        viewModelScope.launch {
            chatEventStore.resetChatClosedCounter()
        }
    }

    override fun onTabVisited(tab: LoggedInTabs) {
        viewModelScope.launch { tabNotificationService.visitTab(tab) }
    }
}
