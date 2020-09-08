package com.hedvig.app.feature.profile.service

import com.mixpanel.android.mpmetrics.MixpanelAPI

class ProfileTracker(
    private val mixpanel: MixpanelAPI
) {
    fun howDoesItWorkClick() = mixpanel.track("CHARITY_INFO_BUTTON_LABEL")
    fun logout() {
        mixpanel.track("PROFILE_LOGOUT_BUTTON")
        mixpanel.reset()
    }

    fun charityRow() = mixpanel.track("PROFILE_ROW_CHARITY_TITLE")
    fun paymentRow() = mixpanel.track("PROFILE_ROW_PAYMENT_TITLE")
    fun myInfoRow() = mixpanel.track("PROFILE_ROW_MY_INFO_TITLE")
    fun feedbackRow() = mixpanel.track("PROFILE_ROW_FEEDBACK_TITLE")
    fun aboutAppRow() = mixpanel.track("PROFILE_ROW_ABOUT_APP_TITLE")
    fun settings() = mixpanel.track("SETTINGS")
}
