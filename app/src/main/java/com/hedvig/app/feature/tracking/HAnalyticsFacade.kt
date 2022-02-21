package com.hedvig.app.feature.tracking

import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.HAnalyticsEvent
import com.hedvig.hanalytics.HAnalyticsExperiment

interface HAnalyticsSink {
    fun send(event: HAnalyticsEvent)
}

interface ExperimentProvider {
    suspend fun getExperiment(name: String): HAnalyticsExperiment
    suspend fun invalidateExperiments()
}

class HAnalyticsFacade(
    private val sinks: List<HAnalyticsSink>,
    private val experimentProvider: ExperimentProvider,
) : HAnalytics() {
    override suspend fun getExperiment(name: String) = experimentProvider.getExperiment(name)
    override suspend fun invalidateExperiments() = experimentProvider.invalidateExperiments()

    override fun send(event: HAnalyticsEvent) {
        sinks.forEach { it.send(event) }
    }
}
