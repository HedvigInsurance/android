package com.hedvig.android.featureflags.test

import app.cash.turbine.Turbine
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Uses a Turbine for the feature, to have more control over what and when it returns a value during a test.
 * Should probably delete `FakeFeatureManager` asap in favor of this, while keeping same API maybe.
 */
class FakeFeatureManager2(
  private val fixedMap: Map<Feature, Boolean> = emptyMap(),
) : FeatureManager {
  /**
   * Allow the feature manager to return [fixedReturnForAll] for all features
   */
  constructor(fixedReturnForAll: Boolean) : this(Feature.entries.associateWith { fixedReturnForAll })

  val featureTurbine = Turbine<Pair<Feature, Boolean>>(name = "FeatureManagerTurbine")

  override fun isFeatureEnabled(feature: Feature): Flow<Boolean> {
    return flow {
      if (fixedMap.containsKey(feature)) {
        emit(fixedMap[feature]!!)
        return@flow
      }
      val pair = featureTurbine.awaitItem()
      require(feature == pair.first)
      emit(pair.second)
    }
  }
}
