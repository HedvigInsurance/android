package com.hedvig.android.hanalytics

import com.hedvig.hanalytics.HAnalyticsEvent

interface HAnalyticsSink {
  fun send(event: HAnalyticsEvent)
  fun identify()
}
