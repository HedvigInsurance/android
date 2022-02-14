package com.hedvig.app.feature.tracking

import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.HAnalyticsEvent

class MockHAnalytics : HAnalytics() {
    override fun send(event: HAnalyticsEvent) = Unit
}
