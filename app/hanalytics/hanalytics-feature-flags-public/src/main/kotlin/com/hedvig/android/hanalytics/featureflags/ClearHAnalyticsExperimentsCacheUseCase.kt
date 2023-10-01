package com.hedvig.android.hanalytics.featureflags

import com.hedvig.android.hanalytics.HAnalyticsExperimentManager

internal class ClearHAnalyticsExperimentsCacheUseCase(
  private val HAnalyticsExperimentManager: HAnalyticsExperimentManager,
) {
  suspend operator fun invoke() {
    HAnalyticsExperimentManager.invalidateExperiments()
  }
}
