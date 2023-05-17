package com.hedvig.android.hanalytics

import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.HAnalyticsEvent
import com.hedvig.hanalytics.HAnalyticsExperiment

internal class HAnalyticsImpl(
  private val sendHAnalyticsEventUseCase: SendHAnalyticsEventUseCase,
  private val hAnalyticsExperimentManager: HAnalyticsExperimentManager,
) : HAnalytics() {
  override suspend fun getExperiment(name: String): HAnalyticsExperiment {
    return hAnalyticsExperimentManager.getExperiment(name)
  }

  override fun identify() {
    sendHAnalyticsEventUseCase.identify()
  }

  override suspend fun invalidateExperiments() {
    hAnalyticsExperimentManager.invalidateExperiments()
  }

  override fun send(event: HAnalyticsEvent) {
    sendHAnalyticsEventUseCase.send(event)
  }
}
