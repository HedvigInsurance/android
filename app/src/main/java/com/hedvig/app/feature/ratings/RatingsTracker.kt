package com.hedvig.app.feature.ratings

import com.hedvig.app.feature.tracking.TrackingFacade

class RatingsTracker(
    private val trackingFacade: TrackingFacade
) {
    fun rate() = trackingFacade.track("rate_app_completed")
}
