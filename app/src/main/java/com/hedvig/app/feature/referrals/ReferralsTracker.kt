package com.hedvig.app.feature.referrals

import com.google.firebase.analytics.FirebaseAnalytics

class ReferralsTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun redeemReferralCode() = firebaseAnalytics.logEvent("REFERRAL_STARTSCREEN_BTN_CTA", null)
    fun skipReferralCode() = firebaseAnalytics.logEvent("REFERRAL_STARTSCREEN_BTN_SKIP", null)
    fun inviteMoreFriends() = firebaseAnalytics.logEvent("REFERRAL_SUCCESS_BTN_CTA", null)
    fun closeReferralSuccess() = firebaseAnalytics.logEvent("REFERRAL_SUCCESS_BTN_CLOSE", null)
    fun redeemReferralCodeOverlay() = firebaseAnalytics.logEvent("REFERRAL_ADDCOUPON_BTN_SUBMIT", null)
}
