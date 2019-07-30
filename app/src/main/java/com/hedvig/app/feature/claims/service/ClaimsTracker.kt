package com.hedvig.app.feature.claims.service

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class ClaimsTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun createClaimClick(origin: String) = firebaseAnalytics.logEvent(
        "create_claim_click",
        Bundle().apply {
            putString("origin", origin)
        })

    fun emergencyClick() = firebaseAnalytics.logEvent("emergency_click", null)
    fun callGlobalAssistance() = firebaseAnalytics.logEvent("call_global_assistance", null)
    fun emergencyChat() = firebaseAnalytics.logEvent("emergency_chat", null)

    fun pledgeHonesty(origin: String?) = firebaseAnalytics.logEvent(
        "honesty_pledge_click",
        Bundle().apply {
            origin?.let { putString("origin", it) }
        }
    )
}
