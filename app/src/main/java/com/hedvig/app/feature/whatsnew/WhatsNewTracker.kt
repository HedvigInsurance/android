package com.hedvig.app.feature.whatsnew

import com.google.firebase.analytics.FirebaseAnalytics
import com.hedvig.app.feature.dismissablepager.DismissablePageTracker

class WhatsNewTracker(
    private val firebaseAnalytics: FirebaseAnalytics
): DismissablePageTracker {
    override fun clickProceed() = firebaseAnalytics.logEvent("NEWS_PROCEED", null)
}
