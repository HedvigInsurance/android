package com.hedvig.app.feature.hanalytics

import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.HAnalyticsEvent
import com.hedvig.hanalytics.HAnalyticsExperiment

class HAnalyticsImpl(
    private val sendHAnalyticsEventUseCase: SendHAnalyticsEventUseCase,
    private val HAnalyticsExperimentManager: HAnalyticsExperimentManager,
) : HAnalytics() {
    override suspend fun getExperiment(name: String): HAnalyticsExperiment {
        return HAnalyticsExperimentManager.getExperiment(name)
    }

    override suspend fun invalidateExperiments() {
        HAnalyticsExperimentManager.invalidateExperiments()
    }

    override fun send(event: HAnalyticsEvent) {
        sendHAnalyticsEventUseCase.send(event)
    }
}
