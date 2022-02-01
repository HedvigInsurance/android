package com.hedvig.app.feature.tracking

import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.HAnalyticsEvent

interface HAnalyticsSink {
    fun send(event: HAnalyticsEvent)
}

class HAnalyticsFacade(
    private val sinks: List<HAnalyticsSink>
) : HAnalytics() {
    override fun send(event: HAnalyticsEvent) {
        sinks.forEach { it.send(event) }
    }
}
