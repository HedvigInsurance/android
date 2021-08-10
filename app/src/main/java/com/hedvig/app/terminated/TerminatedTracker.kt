package com.hedvig.app.terminated

import com.hedvig.app.feature.tracking.TrackingFacade

class TerminatedTracker(
    private val trackingFacade: TrackingFacade,
) {
    fun openChat() = trackingFacade.track("INSURANCE_STATUS_TERMINATED_ALERT_CTA")
}
