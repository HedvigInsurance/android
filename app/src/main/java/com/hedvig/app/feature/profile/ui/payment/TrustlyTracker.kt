package com.hedvig.app.feature.profile.ui.payment

import com.google.firebase.analytics.FirebaseAnalytics

class TrustlyTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun addPaymentInfo() = firebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_PAYMENT_INFO, null)
    fun explainerConnect() {}
}
