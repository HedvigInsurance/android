package com.hedvig.app.feature.loggedin.ui

import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.app.util.jsonObjectOf

class LoggedInTracker(
    private val trackingFacade: TrackingFacade,
) {
    fun setMemberId(memberId: String) = trackingFacade.identify(memberId)
    fun tabVisited(tab: LoggedInTabs) = trackingFacade.track(
        "tab_view",
        jsonObjectOf(
            "tab" to when (tab) {
                LoggedInTabs.HOME -> "home"
                LoggedInTabs.INSURANCE -> "insurance"
                LoggedInTabs.KEY_GEAR -> "key_gear"
                LoggedInTabs.REFERRALS -> "forever"
                LoggedInTabs.PROFILE -> "profile"
            }
        )
    )
}
