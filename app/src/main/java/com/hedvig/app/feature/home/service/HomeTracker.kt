package com.hedvig.app.feature.home.service

import com.mixpanel.android.mpmetrics.MixpanelAPI

class HomeTracker(val mixpanel: MixpanelAPI) {
    fun startClaimOutlined() = mixpanel.track("START_CLAIM_OUTLINED")
    fun startClaimContained() = mixpanel.track("START_CLAIM_CONTAINED")
    fun connectPayin() = mixpanel.track("CONNECT_PAYIN")
    fun psaLink() = mixpanel.track("PSA_LINK")
    fun emergency() = mixpanel.track("EMERGENCY")
    fun openCommonClaimActivity() = mixpanel.track("OPEN_COMMON_CLAIM_ACTIVITY")
    fun retry() = mixpanel.track("RETRY")
}
