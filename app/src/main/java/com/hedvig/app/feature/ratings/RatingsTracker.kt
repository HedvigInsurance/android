package com.hedvig.app.feature.ratings

import com.google.firebase.analytics.FirebaseAnalytics

class RatingsTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    fun doNotLikeApp() = Unit
    fun likeApp() = Unit
    fun noToFeedback() = Unit
    fun yesToFeedback() = Unit
    fun rate() = Unit
    fun doNotRate() = Unit
}
