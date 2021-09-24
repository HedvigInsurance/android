package com.hedvig.app.feature.ratings

import com.hedvig.app.feature.tracking.TrackingFacade
import javax.inject.Inject

class RatingsTracker @Inject constructor(
    private val trackingFacade: TrackingFacade
) {
    fun rate() = trackingFacade.track("RATINGS_DIALOG_BODY_RATE_YES")
    fun doNotRate() = trackingFacade.track("RATINGS_DIALOG_BODY_RATE_NO")
}
