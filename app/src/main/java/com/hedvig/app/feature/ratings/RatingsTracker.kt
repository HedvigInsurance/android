package com.hedvig.app.feature.ratings

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class RatingsTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun doNotLikeApp() = firebaseAnalytics.logEvent("RATINGS_DIALOG_NO", Bundle().apply {
        putString("question", "RATINGS_DIALOG_BODY")
    })

    fun likeApp() = firebaseAnalytics.logEvent("RATINGS_DIALOG_YES", Bundle().apply {
        putString("question", "RATINGS_DIALOG_BODY")
    })

    fun noToFeedback() = firebaseAnalytics.logEvent("RATINGS_DIALOG_NO", Bundle().apply {
        putString("question", "RATINGS_DIALOG_BODY_FEEDBACK")
    })

    fun yesToFeedback() = firebaseAnalytics.logEvent("RATINGS_DIALOG_YES", Bundle().apply {
        putString("question", "RATINGS_DIALOG_BODY_FEEDBACK")
    })

    fun rate() = firebaseAnalytics.logEvent("RATINGS_DIALOG_BODY_RATE_YES", null)
    fun doNotRate() = firebaseAnalytics.logEvent("RATINGS_DIALOG_BODY_RATE_NO", null)
}
