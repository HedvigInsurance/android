package com.hedvig.app.feature.tracking

import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.HAnalyticsEvent
import com.hedvig.hanalytics.HAnalyticsExperiment

class MockHAnalytics : HAnalytics() {
    override suspend fun getExperiment(name: String) = HAnalyticsExperiment(
        "mock",
        "mock",
    )

    override fun identify() = Unit

    override suspend fun invalidateExperiments() = Unit

    override fun send(event: HAnalyticsEvent) = Unit
}
