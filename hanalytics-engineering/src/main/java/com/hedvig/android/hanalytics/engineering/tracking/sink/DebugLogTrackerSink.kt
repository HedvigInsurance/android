package com.hedvig.android.hanalytics.engineering.tracking.sink

import com.hedvig.android.core.common.toJsonObject
import com.hedvig.android.hanalytics.HAnalyticsSink
import com.hedvig.hanalytics.HAnalyticsEvent
import d

/**
 * Sink to assist in seeing where in the app you are navigating while testing and which events are triggered
 * Use by opening AS Logcat on "Debug" mode with the filter "DebugLogTrackerSink"
 */
internal class DebugLogTrackerSink : HAnalyticsSink {
  override fun send(event: HAnalyticsEvent) {
    d { "Track ${event.name}, properties: ${event.properties.toJsonObject().toString(2)}" }
  }

  override fun identify() {
    d { "Identify" }
  }
}
