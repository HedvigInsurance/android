package com.hedvig.android.hanalytics.sink

import com.hedvig.android.core.common.android.toJsonObject
import com.hedvig.android.hanalytics.HAnalyticsSink
import com.hedvig.hanalytics.HAnalyticsEvent
import slimber.log.d

/**
 * Sink to assist in seeing where in the app you are navigating while testing and which events are triggered.
 * Also visible in Firebase Crashlytics, to give a better idea of what lead up to the crash.
 * Use in development by opening AS Logcat on "Debug" mode with the filter "LoggingHAnalyticsSink".
 */
internal class LoggingHAnalyticsSink : HAnalyticsSink {
  override fun send(event: HAnalyticsEvent) {
    d { "Track ${event.name}, properties: ${event.properties.toJsonObject().toString(2)}" }
  }

  override fun identify() {
    d { "\"Identify\" HAnalytics event triggered" }
  }
}
