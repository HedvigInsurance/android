package com.hedvig.android.hanalytics

@InternalHanalyticsApi
interface HAnalyticsService {
  suspend fun getExperiments(): List<Experiment>?
}
