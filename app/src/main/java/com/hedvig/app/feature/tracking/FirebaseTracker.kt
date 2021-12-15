package com.hedvig.app.feature.tracking

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
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

    override fun setProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }
}
