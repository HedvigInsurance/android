package com.hedvig.app.feature.insurance.service

import com.hedvig.app.feature.tracking.TrackingFacade

class InsuranceTracker(
    private val trackingFacade: TrackingFacade,
) {
    fun retry() = trackingFacade.track("home_tab.error_button_text")
}
