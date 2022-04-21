package com.hedvig.app.feature.loggedin.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.feature.chat.data.ChatEventStore
import com.hedvig.app.feature.loggedin.service.TabNotificationService
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

    protected val _unseenTabNotifications = MutableStateFlow<Set<LoggedInTabs>>(emptySet())
    val unseenTabNotifications = _unseenTabNotifications.asStateFlow()

    fun onScroll(scroll: Int) {
        _scroll.postValue(scroll)
    }

    abstract fun onReviewByChatComplete()

    abstract fun onTabVisited(tab: LoggedInTabs)
}

class LoggedInViewModelImpl(
    private val loggedInRepository: LoggedInRepository,
    private val chatEventStore: ChatEventStore,
    private val tabNotificationService: TabNotificationService,
    private val hAnalytics: HAnalytics,
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
        viewModelScope.launch {
            tabNotificationService
                .unseenTabNotifications()
                .collect { unseenTabNotificationSet ->
                    _unseenTabNotifications.value = unseenTabNotificationSet
                }
        }
    }

    override fun onReviewByChatComplete() {
        viewModelScope.launch {
            chatEventStore.resetChatClosedCounter()
        }
    }

    override fun onTabVisited(tab: LoggedInTabs) {
        when (tab) {
            LoggedInTabs.HOME -> hAnalytics.screenViewHome()
            LoggedInTabs.INSURANCE -> hAnalytics.screenViewInsurances()
            LoggedInTabs.KEY_GEAR -> {}
            LoggedInTabs.REFERRALS -> hAnalytics.screenViewForever()
            LoggedInTabs.PROFILE -> hAnalytics.screenViewProfile()
        }
        viewModelScope.launch { tabNotificationService.visitTab(tab) }
    }
}
