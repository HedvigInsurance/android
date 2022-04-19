package com.hedvig.app.feature.loggedin.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.feature.chat.data.ChatEventStore
import com.hedvig.app.feature.loggedin.service.TabNotificationService
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.app.util.featureflags.flags.Feature
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

data class LoggedInViewState(
    val loggedInQueryData: LoggedInQuery.Data,
    val isKeyGearEnabled: Boolean,
    val isForeverEnabled: Boolean,
    val unseenTabNotifications: Set<LoggedInTabs>,
)

abstract class LoggedInViewModel : ViewModel() {
    protected val loggedInQueryData: MutableStateFlow<LoggedInQuery.Data?> = MutableStateFlow(null)
    protected val isKeyGearEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    protected val isForeverEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    protected val unseenTabNotifications: MutableStateFlow<Set<LoggedInTabs>> = MutableStateFlow(emptySet())

    val viewState: StateFlow<LoggedInViewState?> = combine(
        loggedInQueryData.filterNotNull(),
        isKeyGearEnabled,
        isForeverEnabled,
        unseenTabNotifications
    ) { loggedInQueryData, isKeyGearEnabled, isForeverEnabled, unseenTabNotifications ->
        LoggedInViewState(loggedInQueryData, isKeyGearEnabled, isForeverEnabled, unseenTabNotifications)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5.seconds),
            initialValue = null,
        )

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

    abstract fun onTabVisited(tab: LoggedInTabs)
}

class LoggedInViewModelImpl(
    private val loggedInRepository: LoggedInRepository,
    private val chatEventStore: ChatEventStore,
    private val tabNotificationService: TabNotificationService,
    private val featureManager: FeatureManager,
    private val hAnalytics: HAnalytics,
) : LoggedInViewModel() {

    init {
        viewModelScope.launch {
            chatEventStore.observeChatClosedCounter()
                .map { it == 3 }
                .collect(_shouldOpenReviewDialog::tryEmit)
        }
        viewModelScope.launch {
            isKeyGearEnabled.update { featureManager.isFeatureEnabled(Feature.KEY_GEAR) }
            isForeverEnabled.update { featureManager.isFeatureEnabled(Feature.FOREVER) }
        }
        viewModelScope.launch {
            val response = runCatching {
                loggedInRepository.loggedInData()
            }
            response.getOrNull()?.data?.let { loggedInQueryData ->
                this@LoggedInViewModelImpl.loggedInQueryData.update { loggedInQueryData }
            }
        }
        viewModelScope.launch {
            tabNotificationService
                .unseenTabNotifications()
                .collect { unseenTabNotificationSet ->
                    unseenTabNotifications.value = unseenTabNotificationSet
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
