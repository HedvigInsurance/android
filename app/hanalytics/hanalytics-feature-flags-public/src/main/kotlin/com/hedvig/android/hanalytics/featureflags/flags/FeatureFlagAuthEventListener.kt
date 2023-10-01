package com.hedvig.android.hanalytics.featureflags.flags

import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.hanalytics.featureflags.ClearHAnalyticsExperimentsCacheUseCase

internal class FeatureFlagAuthEventListener(
  private val clearHAnalyticsExperimentsCacheUseCase: ClearHAnalyticsExperimentsCacheUseCase,
) : AuthEventListener {
  override suspend fun loggedIn(accessToken: String) {
    clearHAnalyticsExperimentsCacheUseCase.invoke()
  }

  override suspend fun loggedOut() {
    clearHAnalyticsExperimentsCacheUseCase.invoke()
  }
}
