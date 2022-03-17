package com.hedvig.app.feature.hanalytics

import com.hedvig.hanalytics.HAnalyticsEvent

interface HAnalyticsSink {
    fun send(event: HAnalyticsEvent)
}
