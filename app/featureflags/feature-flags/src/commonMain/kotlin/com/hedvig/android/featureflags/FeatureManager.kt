package com.hedvig.android.featureflags

import com.hedvig.android.featureflags.flags.Feature
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow

interface FeatureManager {
  @NativeCoroutines
  fun isFeatureEnabled(feature: Feature): Flow<Boolean>
}
