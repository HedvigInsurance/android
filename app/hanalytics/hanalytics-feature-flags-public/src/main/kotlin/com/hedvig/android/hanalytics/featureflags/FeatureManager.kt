package com.hedvig.android.hanalytics.featureflags

import com.hedvig.android.hanalytics.featureflags.flags.Feature

interface FeatureManager {
  suspend fun isFeatureEnabled(feature: Feature): Boolean
  suspend fun invalidateExperiments()
}
