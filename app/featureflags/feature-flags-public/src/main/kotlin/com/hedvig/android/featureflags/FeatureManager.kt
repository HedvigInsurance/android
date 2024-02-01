package com.hedvig.android.featureflags

import com.hedvig.android.featureflags.flags.Feature
import kotlinx.coroutines.flow.Flow

interface FeatureManager {
  fun isFeatureEnabled(feature: Feature): Flow<Boolean>
}
