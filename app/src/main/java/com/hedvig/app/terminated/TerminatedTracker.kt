package com.hedvig.app.terminated

import com.mixpanel.android.mpmetrics.MixpanelAPI

class TerminatedTracker(
    private val mixpanel: MixpanelAPI
) {
    fun openChat() = mixpanel.track("INSURANCE_STATUS_TERMINATED_ALERT_CTA")
}
