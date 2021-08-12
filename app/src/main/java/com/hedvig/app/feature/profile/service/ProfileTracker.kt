package com.hedvig.app.feature.profile.service

import com.hedvig.app.feature.tracking.TrackingFacade

class ProfileTracker(
    private val trackingFacade: TrackingFacade
) {
    fun howDoesItWorkClick() = trackingFacade.track("CHARITY_INFO_BUTTON_LABEL")
    fun charityRow() = trackingFacade.track("PROFILE_MY_CHARITY_ROW_TITLE")
    fun paymentRow() = trackingFacade.track("PROFILE_ROW_PAYMENT_TITLE")
    fun myInfoRow() = trackingFacade.track("PROFILE_MY_INFO_ROW_TITLE")
    fun aboutAppRow() = trackingFacade.track("PROFILE_ABOUT_ROW")
    fun settings() = trackingFacade.track("profile_appSettingsSection_row_headline")
}
