package com.hedvig.app.feature.loggedin.ui

import com.google.firebase.analytics.FirebaseAnalytics

class LoggedInTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun setMemberId(memberId: String) = firebaseAnalytics.setUserId(memberId)
}
