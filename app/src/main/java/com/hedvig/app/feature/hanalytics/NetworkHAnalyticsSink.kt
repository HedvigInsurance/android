package com.hedvig.app.feature.hanalytics

import com.hedvig.hanalytics.HAnalyticsEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NetworkHAnalyticsSink(
  private val hAnalyticsService: HAnalyticsService,
) : HAnalyticsSink {
  override fun send(event: HAnalyticsEvent) {
    CoroutineScope(Dispatchers.IO).launch {
      hAnalyticsService.sendEvent(event)
    }
  }

  override fun identify() {
    CoroutineScope(Dispatchers.IO).launch {
      hAnalyticsService.identify()
    }
  }
}
