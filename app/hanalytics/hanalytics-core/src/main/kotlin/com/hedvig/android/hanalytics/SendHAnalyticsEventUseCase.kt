package com.hedvig.android.hanalytics

import com.hedvig.hanalytics.HAnalyticsEvent

internal interface SendHAnalyticsEventUseCase {
  fun send(event: HAnalyticsEvent)

  fun identify()
}

internal class SendHAnalyticsEventUseCaseImpl(
  private val sinks: List<HAnalyticsSink>,
) : SendHAnalyticsEventUseCase {
  override fun send(event: HAnalyticsEvent) {
    sinks.forEach { it.send(event) }
  }

  override fun identify() {
    sinks.forEach { it.identify() }
  }
}
