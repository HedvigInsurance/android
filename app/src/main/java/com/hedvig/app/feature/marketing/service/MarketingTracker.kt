package com.hedvig.app.feature.marketing.service

import com.hedvig.app.feature.tracking.TrackingFacade

class MarketingTracker(
    private val trackingFacade: TrackingFacade,
) {
    fun signUp() = trackingFacade.track("begin_onboarding")
    fun logIn() = trackingFacade.track("MARKETING_SCREEN_LOGIN")
}
