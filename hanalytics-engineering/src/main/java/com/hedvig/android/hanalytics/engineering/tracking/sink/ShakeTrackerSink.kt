package com.hedvig.android.hanalytics.engineering.tracking.sink

import com.hedvig.android.hanalytics.HAnalyticsSink
import com.hedvig.hanalytics.HAnalyticsEvent
import com.shakebugs.shake.LogLevel
import com.shakebugs.shake.Shake

internal class ShakeTrackerSink : HAnalyticsSink {
  override fun send(event: HAnalyticsEvent) {
    Shake.log(LogLevel.INFO, "Track: ${event.name}, properties: ${event.properties}")
  }

  override fun identify() {}
}
