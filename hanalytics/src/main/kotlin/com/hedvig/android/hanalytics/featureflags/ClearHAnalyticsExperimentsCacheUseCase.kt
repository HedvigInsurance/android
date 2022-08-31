package com.hedvig.android.hanalytics.featureflags

import com.hedvig.hanalytics.HAnalytics

internal class ClearHAnalyticsExperimentsCacheUseCase(
  private val hAnalytics: HAnalytics,
) {
  suspend operator fun invoke() {
    hAnalytics.invalidateExperiments()
  }
}
