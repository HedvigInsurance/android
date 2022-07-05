package com.hedvig.app.feature.hanalytics

import com.hedvig.hanalytics.HAnalyticsEvent

interface SendHAnalyticsEventUseCase {
  fun send(event: HAnalyticsEvent)
  fun identify()
}

class SendHAnalyticsEventUseCaseImpl(
  private val sinks: List<HAnalyticsSink>,
) : SendHAnalyticsEventUseCase {
  override fun send(event: HAnalyticsEvent) {
    sinks.forEach { it.send(event) }
  }

  override fun identify() {
    sinks.forEach { it.identify() }
  }
}
