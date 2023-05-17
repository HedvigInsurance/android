package com.hedvig.android.hanalytics.sink

import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.hanalytics.HAnalyticsService
import com.hedvig.android.hanalytics.HAnalyticsSink
import com.hedvig.hanalytics.HAnalyticsEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class NetworkHAnalyticsSink(
  private val hAnalyticsService: HAnalyticsService,
  private val applicationScope: ApplicationScope,
) : HAnalyticsSink {
  override fun send(event: HAnalyticsEvent) {
    applicationScope.launch(Dispatchers.IO) {
      hAnalyticsService.sendEvent(event)
    }
  }

  override fun identify() {
    applicationScope.launch(Dispatchers.IO) {
      hAnalyticsService.identify()
    }
  }
}
