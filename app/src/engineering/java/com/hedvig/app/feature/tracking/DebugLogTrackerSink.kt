package com.hedvig.app.feature.tracking

import com.hedvig.app.util.toJsonObject
import com.hedvig.hanalytics.HAnalyticsEvent
import d

/**
 * Sink to assist in seeing where in the app you are navigating while testing and which events are triggered
 * Use by opening AS Logcat on "Debug" mode with the filter "DebugLogTrackerSink"
 */
class DebugLogTrackerSink : HAnalyticsSink {
    override fun send(event: HAnalyticsEvent) {
        d { "Track ${event.name}, properties: ${event.properties.toJsonObject().toString(2)}" }
    }
}
