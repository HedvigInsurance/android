package com.hedvig.app.feature.hanalytics

import com.hedvig.hanalytics.HAnalyticsEvent

interface SendHAnalyticsEventUseCase {
    fun send(event: HAnalyticsEvent)
}

class SendHAnalyticsEventUseCaseImpl(
    private val sinks: List<HAnalyticsSink>,
) : SendHAnalyticsEventUseCase {
    override fun send(event: HAnalyticsEvent) {
        sinks.forEach { it.send(event) }
    }
}
