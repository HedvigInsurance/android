package com.hedvig.app.feature.home.service

import com.mixpanel.android.mpmetrics.MixpanelAPI

class HomeTracker(val mixpanel: MixpanelAPI) {
    fun startClaimOutlined() = mixpanel.track("START_CLAIM_OUTLINED")
    fun startClaimContained() = mixpanel.track("START_CLAIM_CONTAINED")
    fun addPaymentMethod() = mixpanel.track("ADD_PAYMENT_METHOD")
    fun emergency() = mixpanel.track("EMERGENCY")
    fun retry() = mixpanel.track("RETRY")
}
