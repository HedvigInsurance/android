package com.hedvig.app.feature.profile.ui.payment

import com.google.firebase.analytics.FirebaseAnalytics

class TrustlyTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun addPaymentInfo() = firebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_PAYMENT_INFO, null)
    fun explainerConnect() = firebaseAnalytics.logEvent("ONBOARDING_CONNECT_DD_CTA", null)
    fun doItLater() = firebaseAnalytics.logEvent("ONBOARDING_CONNECT_DD_FAILURE_CTA_LATER", null)
    fun retry() = firebaseAnalytics.logEvent("ONBOARDING_CONNECT_DD_FAILURE_CTA_RETRY", null)
    fun notNow() = firebaseAnalytics.logEvent("TRUSTLY_SKIP_BUTTON", null)
}
