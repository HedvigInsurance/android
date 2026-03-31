package com.hedvig.android.featureflags.di

import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class IosFeatureManager(private val isFeatureEnabledBlock: (Feature) -> Boolean) : FeatureManager {
  override fun isFeatureEnabled(feature: Feature): Flow<Boolean> {
    return flowOf(isFeatureEnabledBlock(feature))
  }
}
