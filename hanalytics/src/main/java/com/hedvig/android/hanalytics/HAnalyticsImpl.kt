package com.hedvig.android.hanalytics

import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.HAnalyticsEvent
import com.hedvig.hanalytics.HAnalyticsExperiment
import i

internal class HAnalyticsImpl(
  private val sendHAnalyticsEventUseCase: SendHAnalyticsEventUseCase,
  private val HAnalyticsExperimentManager: HAnalyticsExperimentManager,
) : HAnalytics() {
  override suspend fun getExperiment(name: String): HAnalyticsExperiment {
    val experiment = HAnalyticsExperimentManager.getExperiment(name)
    i { "Experiment requested: $experiment" }
    return experiment
  }

  override fun identify() {
    sendHAnalyticsEventUseCase.identify()
  }

  override suspend fun invalidateExperiments() {
    HAnalyticsExperimentManager.invalidateExperiments()
  }

  override fun send(event: HAnalyticsEvent) {
    sendHAnalyticsEventUseCase.send(event)
  }
}
