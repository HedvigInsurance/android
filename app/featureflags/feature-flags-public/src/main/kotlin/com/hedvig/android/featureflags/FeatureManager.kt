package com.hedvig.android.featureflags

import com.hedvig.android.featureflags.flags.Feature

interface FeatureManager {
  suspend fun isFeatureEnabled(feature: Feature): Boolean
}
