package com.hedvig.app.feature.marketing.service

import com.hedvig.app.feature.tracking.TrackingFacade

class MarketingTracker(
    private val trackingFacade: TrackingFacade,
) {
    fun signUp() = trackingFacade.track("MARKETING_GET_HEDVIG")
    fun logIn() = trackingFacade.track("MARKETING_SCREEN_LOGIN")
}
