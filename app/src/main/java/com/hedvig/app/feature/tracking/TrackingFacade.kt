package com.hedvig.app.feature.tracking

import org.json.JSONObject
import javax.inject.Inject

class TrackingFacade @Inject constructor(
    private val sinks: List<TrackerSink>,
) {
    fun track(eventName: String, properties: JSONObject? = null) {
        sinks.forEach { it.track(eventName, properties) }
    }

    fun identify(id: String) {
        sinks.forEach { it.identify(id) }
    }
}
