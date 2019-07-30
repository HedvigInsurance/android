package com.hedvig.app.feature.welcome

import com.google.firebase.analytics.FirebaseAnalytics
import com.hedvig.app.feature.dismissablepager.DismissablePageTracker

class WelcomeTracker(
    private val firebaseAnalytics: FirebaseAnalytics
) : DismissablePageTracker {
    override fun clickProceed() = firebaseAnalytics.logEvent("NEWS_PROCEED", null)
}
