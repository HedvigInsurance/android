package com.hedvig.android.hanalytics

import com.hedvig.android.core.common.di.LogInfoType
import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.HAnalyticsEvent
import com.hedvig.hanalytics.HAnalyticsExperiment

internal class HAnalyticsImpl(
  private val sendHAnalyticsEventUseCase: SendHAnalyticsEventUseCase,
  private val HAnalyticsExperimentManager: HAnalyticsExperimentManager,
  private val logInfo: LogInfoType,
) : HAnalytics() {
  override suspend fun getExperiment(name: String): HAnalyticsExperiment {
    val experiment = HAnalyticsExperimentManager.getExperiment(name)
    logInfo { "Experiment requested: $experiment" }
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
