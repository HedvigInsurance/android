package com.hedvig.app.feature.hanalytics

import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.HAnalyticsEvent
import com.hedvig.hanalytics.HAnalyticsExperiment

class HAnalyticsImpl(
    private val sendHAnalyticsEventUseCase: SendHAnalyticsEventUseCase,
    private val experimentManager: ExperimentManager,
) : HAnalytics() {
    override suspend fun getExperiment(name: String): HAnalyticsExperiment {
        return experimentManager.getExperiment(name)
    }

    override suspend fun invalidateExperiments() {
        experimentManager.invalidateExperiments()
    }

    override fun send(event: HAnalyticsEvent) {
        sendHAnalyticsEventUseCase.send(event)
    }
}
