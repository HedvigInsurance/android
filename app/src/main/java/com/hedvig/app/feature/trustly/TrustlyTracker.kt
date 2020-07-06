package com.hedvig.app.feature.trustly

import com.mixpanel.android.mpmetrics.MixpanelAPI

class TrustlyTracker(
    private val mixpanel: MixpanelAPI
) {
    fun explainerConnect() = mixpanel.track("ONBOARDING_CONNECT_DD_CTA")
    fun doItLater() = mixpanel.track("ONBOARDING_CONNECT_DD_FAILURE_CTA_LATER")
    fun retry() = mixpanel.track("ONBOARDING_CONNECT_DD_FAILURE_CTA_RETRY")
    fun notNow() = mixpanel.track("TRUSTLY_SKIP_BUTTON")
}
