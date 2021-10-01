package com.hedvig.app.feature.tracking

import com.adyen.checkout.core.model.getStringOrNull
import com.hedvig.app.ScreenTracker
import d
import org.json.JSONObject

/**
 * Sink to assist in seeing where in the app you are navigating while testing and which events are triggered
 * Use by opening AS Logcat on "Debug" mode with the filter "DebugLogTrackerSink"
 */
class DebugLogTrackerSink : TrackerSink {
    override fun track(eventName: String, properties: JSONObject?) {
        d {
            if (eventName == ScreenTracker.SCREEN_VIEW) {
                properties?.getStringOrNull(ScreenTracker.SCREEN_NAME)?.let { screenName ->
                    return@d "Navigated to: $screenName"
                }
            }
            "Track $eventName, properties: $properties"
        }
    }

    override fun identify(id: String) {
        // Not relevant to see in logcat
    }
}
