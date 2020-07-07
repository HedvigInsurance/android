package com.hedvig.app.feature.profile.service

import com.mixpanel.android.mpmetrics.MixpanelAPI

class ProfileTracker(
    private val mixpanel: MixpanelAPI
) {
    fun howDoesItWorkClick() = mixpanel.track("CHARITY_INFO_BUTTON_LABEL")
}
