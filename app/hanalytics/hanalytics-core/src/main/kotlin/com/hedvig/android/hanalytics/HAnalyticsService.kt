package com.hedvig.android.hanalytics

import com.hedvig.hanalytics.HAnalyticsEvent

@InternalHanalyticsApi
interface HAnalyticsService {
  suspend fun sendEvent(event: HAnalyticsEvent)
  suspend fun getExperiments(): List<Experiment>?
  suspend fun identify()
}
