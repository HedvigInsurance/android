package com.hedvig.android.hanalytics.featureflags.flags

import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.hanalytics.featureflags.ClearHAnalyticsExperimentsCacheUseCase
import com.hedvig.android.logger.logcat

internal class FeatureFlagAuthEventListener(
  private val clearHAnalyticsExperimentsCacheUseCase: ClearHAnalyticsExperimentsCacheUseCase,
) : AuthEventListener {
  override suspend fun loggedIn(accessToken: String) {
    logcat { "loggedIn -> clearing loaded experiments" }
    clearHAnalyticsExperimentsCacheUseCase.invoke()
  }

  override suspend fun loggedOut() {
    logcat { "loggedOut -> clearing loaded experiments" }
    clearHAnalyticsExperimentsCacheUseCase.invoke()
  }
}
