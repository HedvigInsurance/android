package com.hedvig.app.feature.tracking

import org.json.JSONObject

class TrackingFacade(
    private val sinks: List<TrackerSink>,
) {
    fun track(eventName: String, properties: JSONObject? = null) {
        sinks.forEach { it.track(eventName, properties) }
    }

    fun identify(id: String) {
        sinks.forEach { it.identify(id) }
    }

    fun setProperty(name: String, value: String) {
        sinks.forEach { it.setProperty(name, value) }
    }
}
