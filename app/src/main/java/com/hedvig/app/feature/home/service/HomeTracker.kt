package com.hedvig.app.feature.home.service

import com.mixpanel.android.mpmetrics.MixpanelAPI

class HomeTracker(private val mixpanel: MixpanelAPI) {
    fun startClaimOutlined() = mixpanel.track("home_tab.claim_button_text")
    fun startClaimContained() = mixpanel.track("home_tab.claim_button_text")
    fun addPaymentMethod() = mixpanel.track("info_card_missing_payment.button_text")
    fun showRenewal() = mixpanel.track("DASHBOARD_RENEWAL_PROMPTER_CTA")
    fun retry() = mixpanel.track("home_tab.error_button_text")
}
