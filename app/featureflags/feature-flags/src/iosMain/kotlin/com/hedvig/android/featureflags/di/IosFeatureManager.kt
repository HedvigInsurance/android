package com.hedvig.android.featureflags.di

import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class IosFeatureManager(private val isFeatureEnabledBlock: (Feature) -> Boolean) : FeatureManager {
  override fun isFeatureEnabled(feature: Feature): Flow<Boolean> = flow {
    emit(withContext(Dispatchers.Main.immediate) { isFeatureEnabledBlock(feature) })
  }
}
