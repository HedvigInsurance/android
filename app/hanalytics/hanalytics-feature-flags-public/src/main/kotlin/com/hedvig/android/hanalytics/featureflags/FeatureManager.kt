package com.hedvig.android.hanalytics.featureflags

import com.hedvig.android.hanalytics.featureflags.flags.FeatureFlagProvider

interface FeatureManager : FeatureFlagProvider {
  suspend fun invalidateExperiments()
}
