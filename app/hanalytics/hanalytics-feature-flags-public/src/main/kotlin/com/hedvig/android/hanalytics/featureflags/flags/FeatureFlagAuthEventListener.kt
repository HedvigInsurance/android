package com.hedvig.android.hanalytics.featureflags.flags

import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.hanalytics.featureflags.FeatureManager

internal class FeatureFlagAuthEventListener(
  private val featureManager: FeatureManager,
) : AuthEventListener {
  override suspend fun loggedIn(accessToken: String) {
    featureManager.invalidateExperiments()
  }

  override suspend fun loggedOut() {
    featureManager.invalidateExperiments()
  }
}
