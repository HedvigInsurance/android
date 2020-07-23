package com.hedvig.app.feature.loggedin.ui

import com.mixpanel.android.mpmetrics.MixpanelAPI

class LoggedInTracker(
    private val mixpanel: MixpanelAPI
) {
    fun setMemberId(memberId: String) = mixpanel.identify(memberId)
}
