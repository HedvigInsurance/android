package com.hedvig.app.feature.embark

import com.hedvig.app.feature.tracking.TrackingFacade
import org.json.JSONObject

class EmbarkTracker(
    private val trackingFacade: TrackingFacade
) {
    fun track(eventName: String, properties: JSONObject? = null) = if (properties != null) {
        trackingFacade.track(eventName, properties)
    } else {
        trackingFacade.track(eventName)
    }
}
