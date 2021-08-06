package com.hedvig.app.feature.tracking

import org.json.JSONObject

interface TrackerSink {
    fun track(eventName: String, properties: JSONObject? = null)
    fun identify(id: String)
}
