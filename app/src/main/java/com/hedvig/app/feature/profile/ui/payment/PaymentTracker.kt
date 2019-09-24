package com.hedvig.app.feature.profile.ui.payment

import com.google.firebase.analytics.FirebaseAnalytics

class PaymentTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun clickRedeemCode() = firebaseAnalytics.logEvent("REFERRAL_ADDCOUPON_HEADLINE", null)
}
