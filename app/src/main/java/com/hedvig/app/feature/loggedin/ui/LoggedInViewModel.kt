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
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

data class LoggedInViewState(
  val loggedInQueryData: LoggedInQuery.Data?,
  val isKeyGearEnabled: Boolean,
  val isReferralsEnabled: Boolean,
  val unseenTabNotifications: Set<LoggedInTabs>,
)

abstract class LoggedInViewModel : ViewModel() {
  abstract val viewState: StateFlow<LoggedInViewState?>

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
  }

  private val loggedInQueryData: Flow<LoggedInQuery.Data?> = flow {
    val loggedInQueryData = loggedInRepository.loggedInData().orNull()
    emit(loggedInQueryData)
  }
  private val isKeyGearEnabled: Flow<Boolean> = flow { emit(featureManager.isFeatureEnabled(Feature.KEY_GEAR)) }
  private val isReferralsEnabled: Flow<Boolean> = flow { emit(featureManager.isFeatureEnabled(Feature.REFERRALS)) }
  override val viewState: StateFlow<LoggedInViewState?> = combine(
    loggedInQueryData,
    isKeyGearEnabled,
    isReferralsEnabled,
    tabNotificationService.unseenTabNotifications(),
  ) { loggedInQueryData, isKeyGearEnabled, isReferralsEnabled, unseenTabNotifications ->
    LoggedInViewState(loggedInQueryData, isKeyGearEnabled, isReferralsEnabled, unseenTabNotifications)
  }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = null,
    )

  override fun onReviewByChatComplete() {
    viewModelScope.launch {
      chatEventStore.resetChatClosedCounter()
    }
  }

  override fun onTabVisited(tab: LoggedInTabs) {
    when (tab) {
      LoggedInTabs.HOME -> hAnalytics.screenView(AppScreen.HOME)
      LoggedInTabs.INSURANCE -> hAnalytics.screenView(AppScreen.INSURANCES)
      LoggedInTabs.KEY_GEAR -> {}
      LoggedInTabs.REFERRALS -> hAnalytics.screenView(AppScreen.FOREVER)
      LoggedInTabs.PROFILE -> hAnalytics.screenView(AppScreen.PROFILE)
    }
    viewModelScope.launch { tabNotificationService.visitTab(tab) }
  }
}
