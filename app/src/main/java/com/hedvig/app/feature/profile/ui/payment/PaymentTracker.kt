package com.hedvig.app.feature.profile.ui.payment

import com.hedvig.app.feature.tracking.TrackingFacade

class PaymentTracker(
    private val trackingFacade: TrackingFacade,
) {
    fun clickRedeemCode() = trackingFacade.track("REFERRAL_ADDCOUPON_HEADLINE")
}
