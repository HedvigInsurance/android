package com.hedvig.app.feature.loggedin.ui

import com.hedvig.app.feature.tracking.TrackingFacade
import javax.inject.Inject

class LoggedInTracker @Inject constructor(
    private val trackingFacade: TrackingFacade,
) {
    fun setMemberId(memberId: String) = trackingFacade.identify(memberId)
}
