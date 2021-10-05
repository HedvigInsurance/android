package com.hedvig.app.feature.tracking

import com.google.firebase.analytics.FirebaseAnalytics
import com.hedvig.app.util.toBundle
import org.json.JSONObject

class FirebaseTracker(
    private val firebaseAnalytics: FirebaseAnalytics,
) : TrackerSink {
    override fun track(eventName: String, properties: JSONObject?) {
        firebaseAnalytics.logEvent(eventName, properties?.toBundle())
    }

    override fun identify(id: String) {
        firebaseAnalytics.setUserId(id)
    }
}
