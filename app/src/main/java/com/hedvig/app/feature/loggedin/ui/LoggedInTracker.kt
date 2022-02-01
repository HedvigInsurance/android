package com.hedvig.app.feature.loggedin.ui

import com.hedvig.app.ScreenTracker
import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.hanalytics.HAnalytics

class LoggedInTracker(
    private val trackingFacade: TrackingFacade,
    private val screenTracker: ScreenTracker,
    private val hAnalytics: HAnalytics,
) {
    fun setMemberId(memberId: String) = trackingFacade.identify(memberId)
    fun tabVisited(tab: LoggedInTabs) {
        if (tab == LoggedInTabs.HOME) {
            hAnalytics.screenViewHome()
        }
        screenTracker.screenView(
            when (tab) {
                LoggedInTabs.HOME -> "home"
                LoggedInTabs.INSURANCE -> "insurances"
                LoggedInTabs.KEY_GEAR -> "key_gear"
                LoggedInTabs.REFERRALS -> "forever"
                LoggedInTabs.PROFILE -> "profile"
            }
        )
    }
}
