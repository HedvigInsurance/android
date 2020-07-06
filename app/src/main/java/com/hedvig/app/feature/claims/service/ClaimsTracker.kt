package com.hedvig.app.feature.claims.service

import com.mixpanel.android.mpmetrics.MixpanelAPI

class ClaimsTracker(
    private val mixpanel: MixpanelAPI
) {
    fun createClaimClick() = mixpanel.track("CLAIMS_CREATE_CLAIM_BUTTON_LABEL")
    fun callGlobalAssistance() = mixpanel.track("CLAIMS_EMERGENCY_SECOND_BOX_BUTTON_LABEL")
    fun emergencyChat() = mixpanel.track("CLAIMS_EMERGENCY_THIRD_BOX_BUTTON_LABEL")
    fun pledgeHonesty() = mixpanel.track("CLAIMS_HONESTY_PLEDGE_BOTTOM_SHEET_BUTTON_LABEL")
}
