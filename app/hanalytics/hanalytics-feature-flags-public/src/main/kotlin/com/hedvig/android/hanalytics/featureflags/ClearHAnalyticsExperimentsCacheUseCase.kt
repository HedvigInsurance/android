package com.hedvig.android.hanalytics.featureflags

import com.hedvig.android.hanalytics.HAnalyticsExperimentClearUseCase

internal class ClearHAnalyticsExperimentsCacheUseCase(
  private val hAnalyticsExperimentClearUseCase: HAnalyticsExperimentClearUseCase,
) {
  suspend operator fun invoke() {
    hAnalyticsExperimentClearUseCase.invoke()
  }
}
