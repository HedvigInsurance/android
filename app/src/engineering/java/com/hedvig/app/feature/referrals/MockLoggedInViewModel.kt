package com.hedvig.app.feature.referrals

import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.loggedin.ui.LoggedInViewState
import com.hedvig.app.testdata.feature.referrals.builders.LoggedInDataBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockLoggedInViewModel : LoggedInViewModel() {

  override val viewState: StateFlow<LoggedInViewState?> = MutableStateFlow(
    LoggedInViewState(
      loggedInQueryData = LoggedInDataBuilder().build(),
      isKeyGearEnabled = true,
      isReferralsEnabled = true,
      unseenTabNotifications = emptySet(),
    ),
  )

  override fun onReviewByChatComplete() {}
  override fun onTabVisited(tab: LoggedInTabs) {}
}
