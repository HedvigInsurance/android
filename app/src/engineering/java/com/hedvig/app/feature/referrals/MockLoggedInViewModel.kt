package com.hedvig.app.feature.referrals

import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.testdata.feature.referrals.builders.LoggedInDataBuilder
import kotlinx.coroutines.flow.update

class MockLoggedInViewModel : LoggedInViewModel() {
    init {
        loggedInQueryData.update { LoggedInDataBuilder().build() }
    }

    override fun onReviewByChatComplete() {}
    override fun onTabVisited(tab: LoggedInTabs) {}
}
