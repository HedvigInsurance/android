package com.hedvig.app.feature.tracking

import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.HAnalyticsEvent
import com.hedvig.hanalytics.HAnalyticsExperiment

interface HAnalyticsSink {
    fun send(event: HAnalyticsEvent)
}

class HAnalyticsFacade(
    private val sinks: List<HAnalyticsSink>
) : HAnalytics() {
    override fun send(event: HAnalyticsEvent) {
        sinks.forEach { it.send(event) }
    }

    override suspend fun getExperiment(name: String): HAnalyticsExperiment {
        // no-op until we merge the feature flag branch. It's not called anywhere.
        return HAnalyticsExperiment("test", "test")
    }

    override fun identify() {
        // no-op until we merge the feature flag branch
    }

    override suspend fun invalidateExperiments() {
        // no-op until we merge the feature flag branch
    }
}
