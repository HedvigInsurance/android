package com.hedvig.app.feature.home.service

import com.hedvig.app.feature.tracking.TrackingFacade

class HomeTracker(
    private val trackingFacade: TrackingFacade
) {
    fun startClaimOutlined() = trackingFacade.track("home_tab.claim_button_text")
    fun startClaimContained() = trackingFacade.track("home_tab.claim_button_text")
    fun addPaymentMethod() = trackingFacade.track("info_card_missing_payment.button_text")
    fun showRenewal() = trackingFacade.track("DASHBOARD_RENEWAL_PROMPTER_CTA")
    fun retry() = trackingFacade.track("home_tab.error_button_text")
}
