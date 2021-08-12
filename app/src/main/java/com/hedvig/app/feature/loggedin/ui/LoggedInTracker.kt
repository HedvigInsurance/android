package com.hedvig.app.feature.loggedin.ui

import com.hedvig.app.feature.tracking.TrackingFacade

class LoggedInTracker(
    private val trackingFacade: TrackingFacade,
) {
    fun setMemberId(memberId: String) = trackingFacade.identify(memberId)
}
