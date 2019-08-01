package com.hedvig.app.authenticate

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AuthTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun login() = firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
        putString(FirebaseAnalytics.Param.METHOD, "bankid")
    })
}
