package com.hedvig.app.feature.profile.service

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class ProfileTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun clickReferral(incentive: Int?) = firebaseAnalytics.logEvent(
        "click_referral",
        Bundle().apply {
            incentive?.let { putInt("incentive", it) }
        }
    )

    fun howDoesItWorkClick() = firebaseAnalytics.logEvent("click_charity_how_does_it_work", null)
}
