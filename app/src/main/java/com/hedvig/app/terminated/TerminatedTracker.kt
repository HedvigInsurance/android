package com.hedvig.app.terminated

import com.google.firebase.analytics.FirebaseAnalytics

class TerminatedTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun openChat() = firebaseAnalytics.logEvent("INSURANCE_STATUS_TERMINATED_ALERT_CTA", null)
}
