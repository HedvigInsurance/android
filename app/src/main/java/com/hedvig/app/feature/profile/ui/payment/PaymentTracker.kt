package com.hedvig.app.feature.profile.ui.payment

import com.mixpanel.android.mpmetrics.MixpanelAPI

class PaymentTracker(
    private val mixpanel: MixpanelAPI
) {
    fun clickRedeemCode() = mixpanel.track("REFERRAL_ADDCOUPON_HEADLINE")
}
