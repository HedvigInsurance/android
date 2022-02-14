package com.hedvig.app.feature.tracking

import com.hedvig.hanalytics.HAnalyticsEvent
import com.shakebugs.shake.LogLevel
import com.shakebugs.shake.Shake

class ShakeTrackerSink : HAnalyticsSink {
    override fun send(event: HAnalyticsEvent) {
        Shake.log(LogLevel.INFO, "Track: ${event.name}, properties: ${event.properties}")
    }
}
