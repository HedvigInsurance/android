package com.hedvig.app.feature.referrals

import com.mixpanel.android.mpmetrics.MixpanelAPI

class ReferralsTracker(
    private val mixpanel: MixpanelAPI
) {
    fun redeemReferralCode() = mixpanel.track("REFERRAL_STARTSCREEN_BTN_CTA")
    fun skipReferralCode() = mixpanel.track("REFERRAL_STARTSCREEN_BTN_SKIP")
    fun redeemReferralCodeOverlay() = mixpanel.track("REFERRAL_ADDCOUPON_BTN_SUBMIT")
}
