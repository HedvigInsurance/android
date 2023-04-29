package com.hedvig.android.hanalytics.test

import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.HAnalyticsEvent
import com.hedvig.hanalytics.HAnalyticsExperiment

class FakeHAnalytics : HAnalytics() {
  override suspend fun getExperiment(name: String): HAnalyticsExperiment {
    println("FakeHAnalytics: getExperiment: name:$name")
    return HAnalyticsExperiment("Test", "Test")
  }

  override fun identify() {
    println("FakeHAnalytics: identify")
  }

  override suspend fun invalidateExperiments() {
    println("FakeHAnalytics: invalidateExperiments")
  }

  override fun send(event: HAnalyticsEvent) {
    println("FakeHAnalytics: send: event:$event")
  }
}
