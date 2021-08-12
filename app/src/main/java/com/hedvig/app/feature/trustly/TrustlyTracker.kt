package com.hedvig.app.feature.trustly

import com.hedvig.app.feature.tracking.TrackingFacade

class TrustlyTracker(
    private val trackingFacade: TrackingFacade,
) {
    fun doItLater() = trackingFacade.track("ONBOARDING_CONNECT_DD_FAILURE_CTA_LATER")
    fun retry() = trackingFacade.track("ONBOARDING_CONNECT_DD_FAILURE_CTA_RETRY")
    fun close() = trackingFacade.track("PROFILE_TRUSTLY_CLOSE")
}
