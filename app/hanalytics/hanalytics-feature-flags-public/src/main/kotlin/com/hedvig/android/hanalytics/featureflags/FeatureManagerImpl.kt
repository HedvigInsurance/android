package com.hedvig.android.hanalytics.featureflags

import com.hedvig.android.hanalytics.featureflags.flags.FeatureFlagProvider

internal class FeatureManagerImpl(
  private val featureFlagProvider: FeatureFlagProvider,
  private val clearHAnalyticsExperimentsCacheUseCase: ClearHAnalyticsExperimentsCacheUseCase,
) : FeatureManager, FeatureFlagProvider by featureFlagProvider {
  override suspend fun invalidateExperiments() {
    clearHAnalyticsExperimentsCacheUseCase.invoke()
  }
}
