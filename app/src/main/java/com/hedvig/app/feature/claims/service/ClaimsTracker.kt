package com.hedvig.app.feature.claims.service

import com.hedvig.app.feature.tracking.TrackingFacade

class ClaimsTracker(
    private val trackingFacade: TrackingFacade
) {
    fun callGlobalAssistance() = trackingFacade.track("CLAIMS_EMERGENCY_SECOND_BOX_BUTTON_LABEL")
    fun emergencyChat() = trackingFacade.track("CLAIMS_EMERGENCY_THIRD_BOX_BUTTON_LABEL")
    fun pledgeHonesty() = trackingFacade.track("CLAIMS_HONESTY_PLEDGE_BOTTOM_SHEET_BUTTON_LABEL")
}
