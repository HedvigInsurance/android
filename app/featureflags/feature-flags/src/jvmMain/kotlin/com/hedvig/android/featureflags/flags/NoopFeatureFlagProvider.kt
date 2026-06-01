package com.hedvig.android.featureflags.flags

import com.hedvig.android.featureflags.FeatureManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class NoopFeatureFlagProvider : FeatureManager {
  override fun isFeatureEnabled(feature: Feature): Flow<Boolean> {
    return flowOf(false)
  }
}
