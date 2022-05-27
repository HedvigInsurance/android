package com.hedvig.app.util.featureflags

import com.hedvig.hanalytics.HAnalytics

class ClearHAnalyticsExperimentsCacheUseCase(
    private val hAnalytics: HAnalytics,
) {
    suspend operator fun invoke() {
        hAnalytics.invalidateExperiments()
    }
}
