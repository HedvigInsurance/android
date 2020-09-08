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

    fun charityRow() = mixpanel.track("PROFILE_MY_CHARITY_ROW_TITLE")
    fun paymentRow() = mixpanel.track("PROFILE_ROW_PAYMENT_TITLE")
    fun myInfoRow() = mixpanel.track("PROFILE_MY_INFO_ROW_TITLE")
    fun feedbackRow() = mixpanel.track("PROFILE_ROW_FEEDBACK_TITLE")
    fun aboutAppRow() = mixpanel.track("PROFILE_ABOUT_ROW")
    fun settings() = mixpanel.track("profile_appSettingsSection_row_headline")
}
